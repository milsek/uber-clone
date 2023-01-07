from locust import task, between, HttpUser
import random

current_user_count = 1


class Driver(HttpUser):
    wait_time = between(65, 95)
    credentials = None

    def on_start(self):
        global current_user_count
        response = self.client.post("/api/auth/custom-login", json={"username": 'driver' + str(current_user_count),
                                                                    "password": "cascaded"})
        current_user_count += 1
        if response.text:
            self.credentials = response.json()['accessToken']
            self.hello()

    @task
    def hello(self):
        if self.credentials:
            response = self.client.post("/api/simulator", headers={"Authorization": "Bearer " + self.credentials})
