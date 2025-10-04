# wallet

<h3>Build Project</h3>
To Build and Start the project with it's dependencies (MySQL and Redis) just run below command :
</br><b>docker-compose up --build wallet-service</b>

<h3>APIS</h3>
This wallet service has these 7 apis , You can also access them using swagger in address :
</br> <b>http://localhost:8080/swagger-ui.html </b>

1. <b>Register User :</b>
<br/>(No Authentication Required)
  <br/>   curl --location 'http://localhost:8080/v1/auth/register' \
   --header 'accept: */*' \
   --header 'Content-Type: application/json' \
   --data '{
   "phoneNumber": "09225078200",
   "firstName": "zahhhh",
   "lastName": "ssssss",
   "password": "12345678"
   }'
   <br/> <br/>
2. <b>Login User :</b>
   <br/>(No Authentication Required)
   <br/>  curl --location 'http://localhost:8080/v1/auth/login' \
   --header 'accept: */*' \
   --header 'Content-Type: application/json' \
   --data '{
   "phoneNumber": "09104916481",
   "password":"12345678"
}'
   <br/> <br/>
3. <b>Get Balance Of Wallet : </b>
   <br/>(JWT Token Authentication is Required)
   <br/>curl --location 'http://localhost:8080/v1/wallet/1/balance' \
   --header 'Authorization: Bearer {JWT_TOKEN}'
</br></br>
4. <b>Get Transactions of Wallet :</b>
<br/>(JWT Token Authentication is Required)
   curl --location 'http://localhost:8080/v1/wallet/1/transactions' \
   --header 'Authorization: Bearer {JWT_TOKEN}'
</br></br>
5. <b>ToUp to a Wallet :</b>
</br>(JWT Token Authentication is Required)
   </br>curl --location 'http://localhost:8080/v1/wallet/1/top-up' \
   --header 'Content-Type: application/json' \
   --header 'Authorization: Bearer {JWT_TOKEN}' \
   --data '{
   "amount" : 500000.00,
   "paymentReference" : "test"
   }'
</br></br>
6. <b>Withdraw from a Wallet :</b>
</br>(JWT Token Authentication is Required)
</br>curl --location 'http://localhost:8080/v1/wallet/1/withdraw' \
   --header 'Content-Type: application/json' \
   --header 'Authorization: Bearer {JWT_TOKEN}' \
   --data '{
   "amount" : 100000.00
   }'
</br></br>
7. <b>Transfer from one Wallet to another Wallet :</b>
</br>(JWT Token Authentication is Required):
</br>curl --location 'http://localhost:8080/v1/wallet/transfer' \
   --header 'Content-Type: application/json' \
   --header 'Authorization: Bearer {JWT_TOKEN}}' \
   --data '{
   "fromWalletId" : 1,
   "toWalletId" : 2,
   "amount" : 120000.00
   }'
