# Suber (uber clone)
This is a uber clone web application built for UNI courses *Advanced web technologies* and *Software testing*.

### Running the backend application locally
Run the application via **IntelliJ IDEA**. The project runs on **Java 17**.

### Running the frontend application locally on a dev server
First of all, in order to install the required dependencies run
```sh
npm install
```
To start the Angular development server server run
```sh
ng serve
```
Then, navigate to `http://localhost:4200/`.

In order to run Angular tests, run `ng test`.

### Running the simulator script
Navigate to the `/simulator` directory, create a python virtualenv called `venv`, install the dependencies using `pip install -r requirements.txt`, and then run the `run.bat` script (on Windows), or alternatively activate the virtualenv manually and run
```sh
locust --config=config.conf
```