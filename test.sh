#!/usr/bin/env bash
echo "GET /unterkuenfte/"
curl -X GET "http://localhost:8080/unterkuenfte" -H  "accept: application/json;charset=UTF-8"
echo " "
curl -X GET "http://localhost:8080/unterkuenfte?top=1" -H  "accept: application/json;charset=UTF-8"
echo " "
curl -X GET "http://localhost:8080/unterkuenfte?bezeichnung=Vi&top=3" -H  "accept: application/json;charset=UTF-8"
echo " "
#

echo "GET /fluege/"
curl -X GET "http://localhost:8080/fluege?flugzeugid=1" -H  "accept: application/json;charset=UTF-8"
echo " "
curl -X GET "http://localhost:8080/fluege?zielflughafen=BER" -H  "accept: application/json;charset=UTF-8"
echo " "
#

echo "GET /flughaefen/"
curl -X GET "http://localhost:8080/flughaefen?bezeichnung=fr" -H  "accept: application/json;charset=UTF-8"
echo " "
#

echo "GET /flugzeuge/"
curl -X GET "http://localhost:8080/flugzeuge?modell=i" -H  "accept: application/json;charset=UTF-8"
echo " "
curl -X GET "http://localhost:8080/flugzeuge?modell=emb&passagieranzahl=2&crewanzahl=2" -H  "accept: application/json;charset=UTF-8"
echo ""
#

echo "GET /reisebueros/"
curl -X GET "http://localhost:8080/reisebueros?username=pet" -H  "accept: application/json;charset=UTF-8"
echo " "
curl -X GET "http://localhost:8080/reisebueros?email=ha" -H  "accept: application/json;charset=UTF-8"
echo ""
curl -X GET "http://localhost:8080/reisebueros?username=ha&email=ha" -H  "accept: application/json;charset=UTF-8"
echo ""
#

echo "POST /reisebueros/"
RAN=$RANDOM
curl -X POST "http://localhost:8080/reisebueros" -H  "accept: */*" -H  "Content-Type: multipart/form-data" -F "username=test$RAN" -F "email=test$RAN@test.de" -F "passwort=password$RAN" -F "adresseid=1"
echo ""
curl -X POST "http://localhost:8080/reisebueros" -H  "accept: */*" -H  "Content-Type: multipart/form-data" -F "username=test$RAN" -F "email=test$RAN@test.de" -F "passwort=password$RAN" -F "adresseid=1"
echo ""
curl -X POST "http://localhost:8080/reisebueros" -H  "accept: */*" -H  "Content-Type: multipart/form-data" -F "username=test$RAN" -F "email=test$RAN@test.de" -F "adresseid=1"
echo ""
curl -X GET "http://localhost:8080/reisebueros?username=test" -H  "accept: application/json;charset=UTF-8"
echo ""
##############################################################

echo "AUTH GET /buchungen/"
curl -X GET "http://localhost:8080/buchungen" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
curl -X GET "http://localhost:8080/buchungen?username=peter" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
curl -X GET "http://localhost:8080/buchungen?email=peter@gmail.com" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
curl -X GET "http://localhost:8080/buchungen?email=peter@gmail.com&username=peter" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
#

echo "AUTH GET /flugtickets"
curl -X GET "http://localhost:8080/flugtickets?vorname=f&nachname=g" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
curl -X GET "http://localhost:8080/flugtickets?vorname=f" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
curl -X GET "http://localhost:8080/flugtickets?nachname=g" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
curl -X GET "http://localhost:8080/flugtickets" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
#

echo "AUTH POST /flugtickets/"
RAN=$RANDOM
curl -X POST "http://localhost:8080/flugtickets" -H  "accept: */*" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA==" -H  "Content-Type: multipart/form-data" -F "vorname=Obo$RAN" -F "nachname=Kolo$RAN" -F "geschlecht=w" -F "gepaeck=true" -F "extragepaeck=false" -F "flugid=3" -F "preis=100"
echo ""
curl -X GET "http://localhost:8080/flugtickets" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""

echo "AUTH DELETE /flugtickets/"
curl -X DELETE "http://localhost:8080/flugtickets/73" -H  "accept: */*" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
curl -X DELETE "http://localhost:8080/flugtickets/5" -H  "accept: */*" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
#

echo "AUTH GET flugtickets/X/fluege"
curl -X GET "http://localhost:8080/flugtickets/4/fluege" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
curl -X GET "http://localhost:8080/flugtickets/99999/fluege" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
#

echo "AUTH POST flugtickets/X/fluege"
curl -X POST "http://localhost:8080/flugtickets/7/fluege" -H  "accept: */*" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA==" -H  "Content-Type: multipart/form-data" -F "flugid=9"
echo ""
#

echo "AUTH DELETE reisen/reiseid"
curl -X DELETE "http://localhost:8080/reisen/52" -H  "accept: */*" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
#

echo "AUTH GET /reisen"
curl -X GET "http://localhost:8080/reisen?titel=a" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
curl -X GET "http://localhost:8080/reisen?titel=a&startdatum=2019-05-12" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
curl -X GET "http://localhost:8080/reisen?titel=a&startdatum=2019-05-12&tags=FoodANDFun" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
#

echo "AUTH POST /reisen"
curl -X POST "http://localhost:8080/reisen" -H  "accept: */*" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA==" -H  "Content-Type: multipart/form-data" -F "startzeitpunkt=2020-05-05 10:10:10" -F "dauer=100" -F "titel=Titell" -F "unterkunftid=1" -F "preis=100"
echo ""
#

echo "AUTH GET /reisen/{reiseid}/unterkuenfte"
curl -X GET "http://localhost:8080/reisen/27/unterkuenfte" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
#

echo "AUTH POST /reisen/{reiseid}/unterkuenfte"
curl -X POST "http://localhost:8080/reisen/4/tags" -H  "accept: */*" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA==" -H  "Content-Type: multipart/form-data" -F "tag=FunANDFood"
echo ""
#

echo "AUTH GET /reisen/{reiseid}/unterkuenfte"
curl -X GET "http://localhost:8080/reisen/4/tags" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
#

echo "AUTH POST /reisen​/{reiseid}​/tags"
curl -X POST "http://localhost:8080/reisen/4/tags" -H  "accept: */*" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA==" -H  "Content-Type: multipart/form-data" -F "tag=Fun"
echo ""
curl -X GET "http://localhost:8080/reisen/51/tags" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
#

echo "AUTH GET /reisen​/{reiseid}​/tags"
curl -X GET "http://localhost:8080/reisen/24/tags" -H  "accept: application/json;charset=UTF-8" -H  "Authorization: Basic cGV0ZXI6d29yc2Rmc2RmdA=="
echo ""
