# WheelTime 

## käivita docker
* docker run -d -p 9003:80 surmus/london-tire-workshop:2.0.1
* docker run -d -p 9004:80 surmus/manchester-tire-workshop:2.0.1


##  backend java version 21

* käivita backend
* ./gradlew bootRun
* või run WheelTimeApplication otse
* või shift +F10



## frontend vue 3

* Installeeri axios API kutsede tegemiseks
* npm install axios

* Vue 3 jaoks pead kasutama Vuex 4 
* npm install vuex@next

* Veenduge, et API kutsete URL-id suunavad (http://localhost:8080/api).

## käivita fronend käsuga

* cd frontend
* npm run serve
*



### andmebaasi setup

baasi seadistamiseks on URL, port, user ja password
src/main/resources/application.properties
