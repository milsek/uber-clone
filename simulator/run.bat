@echo off
CALL .\venv\Scripts\activate
locust --config=config.conf
deactivate
pause