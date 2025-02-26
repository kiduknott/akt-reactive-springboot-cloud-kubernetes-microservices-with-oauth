#!/usr/bin/env bash
#
# Sample usage:
#
# ./test-all-microservices-on-docker-via-product-composite.bash start stop
#
: ${HOST=localhost}
: ${PORT=8080}
: ${PROD_ID_RETURN_REVIEWS_RECOMMENDATIONS=1}
: ${PROD_ID_NOT_FOUND=123}
: ${PROD_ID_NO_RECOMMENDATIONS=113}
: ${PROD_ID_NO_REVIEWS=213}
: ${MAX_RETRIES=60}

function assertCurl() {

  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]
  then
    if [ "$httpCode" = "200" ]
    then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
  else
    echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
    echo  "- Failing command: $curlCmd"
    echo  "- Response Body: $RESPONSE"
    exit 1
  fi
}

function assertEqual() {

  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]
  then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    exit 1
  fi
}

function assertContains() {

  local expected=$1
  local actual=$2

  if [[ "$actual" == *"$expected"* ]]
  then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED TO CONTAIN VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    exit 1
  fi
}

function testUrl() {
  url=$@
  if $url -ks -f -o /dev/null
  then
    return 0
  else
    return 1
  fi;
}

function waitForService() {
  url=$@
  echo -n "Wait for: $url... "
  n=0
  until testUrl $url
  do
    n=$((n + 1))
    if [[ $n == $MAX_RETRIES ]]
    then
      echo "Retries timed out..."
      exit 1
    else
      sleep 1
      echo -n ", retry #$n "
    fi
  done
  echo "DONE, continuing..."
}

function recreateComposite() {
  local productId=$1
  local composite=$2

  assertCurl 202 "curl -X DELETE http://$HOST:$PORT/product-composite/${productId} -s"
  assertEqual 202 $(curl -X POST -s http://$HOST:$PORT/product-composite -H "Content-Type: application/json" --data "$composite" -w "%{http_code}")
}

function setupTestdata() {

  echo "Started - Seeding test data ..."

  body="{\"productId\":$PROD_ID_NO_RECOMMENDATIONS"
  body+=\
',"name":"product name A","weight":100, "reviews":[
  {"reviewId":1,"author":"author 1","subject":"subject 1","content":"content 1"},
  {"reviewId":2,"author":"author 2","subject":"subject 2","content":"content 2"},
  {"reviewId":3,"author":"author 3","subject":"subject 3","content":"content 3"}
]}'
  recreateComposite "$PROD_ID_NO_RECOMMENDATIONS" "$body"

  body="{\"productId\":$PROD_ID_NO_REVIEWS"
  body+=\
',"name":"product name B","weight":200, "recommendations":[
  {"recommendationId":1,"author":"author 1","rating":1,"content":"content 1"},
  {"recommendationId":2,"author":"author 2","rating":2,"content":"content 2"},
  {"recommendationId":3,"author":"author 3","rating":3,"content":"content 3"}
]}'
  recreateComposite "$PROD_ID_NO_REVIEWS" "$body"


  body="{\"productId\":$PROD_ID_RETURN_REVIEWS_RECOMMENDATIONS"
  body+=\
',"name":"product name C","weight":300, "recommendations":[
      {"recommendationId":1,"author":"author 1","rating":1,"content":"content 1"},
      {"recommendationId":2,"author":"author 2","rating":2,"content":"content 2"},
      {"recommendationId":3,"author":"author 3","rating":3,"content":"content 3"}
  ], "reviews":[
      {"reviewId":1,"author":"author 1","subject":"subject 1","content":"content 1"},
      {"reviewId":2,"author":"author 2","subject":"subject 2","content":"content 2"},
      {"reviewId":3,"author":"author 3","subject":"subject 3","content":"content 3"}
  ]}'
  recreateComposite "$PROD_ID_RETURN_REVIEWS_RECOMMENDATIONS" "$body"

  echo "Completed - Test data seeded"
}

function waitForMessagesToProcess(){
  echo "Waiting for message processing to complete..."

  # Give background processing some time to complete...
  sleep 1

  until testCompositeCreated
  do
    n=$((n + 1))
    if [[ $n == $MAX_RETRIES ]]
    then
      echo "Retries timed out..."
      exit 1
    else
      sleep 1
      echo -n ", retry #$n "
    fi
  done
  echo "DONE - Waiting for messages processing..."
}

function testCompositeCreated(){
  if ! assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_RETURN_REVIEWS_RECOMMENDATIONS -s"
  then
    echo -n "Fail - Call to get product composite did not return Http 200"
    return 1
  fi

  #TODO: FIX - Something wrong with Kafka messaging such that 200 OK is returned before reviews are added to the response
  sleep 5

  assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_RETURN_REVIEWS_RECOMMENDATIONS -s"

  echo -e "[0]. \n $RESPONSE"

  set +e

  assertEqual "$PROD_ID_RETURN_REVIEWS_RECOMMENDATIONS" $(echo $RESPONSE | jq .productId)
  if [ "$?" -eq "1" ]
  then
    return 1
  fi

  assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
  if [ "$?" -eq "1" ]
  then
    return 1
  fi

  assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")
  if [ "$?" -eq "1" ]
  then
    return 1
  fi

  set -e

}

set -e

echo "Start Tests:" `date`

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]
then
  echo "Restarting the test environment..."
  echo "$ docker compose down --remove-orphans"
  docker compose down --remove-orphans
  echo "$ docker compose up -d"
  docker compose up -d
fi

waitForService curl http://$HOST:$PORT/actuator/health

# Verify access to Eureka and that all four microservices are registered in Eureka
assertCurl 200 "curl -H "accept:application/json" $HOST:$PORT/eureka/apps -s"
assertEqual 5 $(echo $RESPONSE | jq ".applications.application | length")

setupTestdata

waitForMessagesToProcess

# Verify that a normal request works, expect three recommendations and three reviews
assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_RETURN_REVIEWS_RECOMMENDATIONS -s"
echo -e "[1].\n$RESPONSE"
assertEqual $PROD_ID_RETURN_REVIEWS_RECOMMENDATIONS $(echo $RESPONSE | jq .productId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

# Verify that a 404 (Not Found) error is returned for a non-existing productId ($PROD_ID_NOT_FOUND)
assertCurl 404 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NOT_FOUND -s"
echo -e "[2].\n$RESPONSE"
assertContains "No product found for productId: $PROD_ID_NOT_FOUND" "$(echo $RESPONSE | jq -r .message)"

# Verify that no recommendations are returned for productId $PROD_ID_NO_RECOMMENDATIONS
assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NO_RECOMMENDATIONS -s"
echo -e "[3].\n$RESPONSE"
assertEqual $PROD_ID_NO_RECOMMENDATIONS $(echo $RESPONSE | jq .productId)
assertEqual 0 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".reviews | length")

# Verify that no reviews are returned for productId $PROD_ID_NO_REVIEWS
assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NO_REVIEWS -s"
echo -e "[4].\n$RESPONSE"
assertEqual $PROD_ID_NO_REVIEWS $(echo $RESPONSE | jq .productId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a productId that is out of range (-1)
assertCurl 422 "curl http://$HOST:$PORT/product-composite/-1 -s"
echo -e "[5].\n$RESPONSE"
assertContains "Invalid productId: -1" "$(echo $RESPONSE | jq .message)"

# Verify that a 400 (Bad Request) error error is returned for a productId that is not a number, i.e. invalid format
assertCurl 400 "curl http://$HOST:$PORT/product-composite/invalidProductId -s"
echo -e "[6].\n$RESPONSE"
assertContains "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

# Verify access to Swagger and OpenAPI urls
echo "Swagger and OpenAPI tests"
assertCurl 302 "curl -s http://$HOST:$PORT/openapi/swagger-ui.html"
assertCurl 200 "curl -sL http://$HOST:$PORT/openapi/swagger-ui.html"
assertCurl 200 "curl -s http://$HOST:$PORT/openapi/webjars/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config"
assertCurl 200 "curl -s http://$HOST:$PORT/openapi/v3/api-docs"
assertEqual "3.0.1" "$(echo $RESPONSE | jq -r .openapi)"
assertEqual "http://$HOST:$PORT" "$(echo $RESPONSE | jq -r '.servers[0].url')"
assertCurl 200 "curl -s http://$HOST:$PORT/openapi/v3/api-docs.yaml"

if [[ $@ == *"stop"* ]]
then
    echo "Tests completed OK. Stopping the test environment..."
    echo "$ docker compose down"
    docker compose down
fi

echo "End, all tests OK:" `date`
