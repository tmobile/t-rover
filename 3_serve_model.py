import cv2
import numpy as np
from PIL import Image
# import Image
import glob
import pandas as pd
from sklearn.model_selection import train_test_split
import math
from sklearn import preprocessing

import cv2
import time
import serial

from keras.models import load_model

WIDTH = 75
HEIGHT = 100

# video_url = 'http://192.168.1.176:8080/video'
arduino_port = '/dev/cu.wchusbserial1440'
arduino_port_2 = '/dev/cu.wchusbserial1430'

video_url = 'http://192.168.43.1:8080/video'

try:
    ser = serial.Serial(arduino_port, 57600)
except:
    ser = serial.Serial(arduino_port_2, 57600)
cap = cv2.VideoCapture(video_url)
model = load_model('models/model1.h5')

ret, image = cap.read()
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
image_small = cv2.resize(gray, (WIDTH, HEIGHT))
arr = model.predict(np.asarray(image_small).reshape(1,WIDTH,HEIGHT,1))
accel = int(arr[0][0] * 128)
steer = int(arr[0][1] * 128)
print(f"accel {accel}, steeer {steer}")


ser.write(f"dmx".encode())
ser.write("a66xs67x".encode())
line = ser.readline().decode('ascii')
print(f"setting line {line}")
ser.write(f"px".encode())

line = ser.readline().decode('ascii')
print(line)

count = 0
while True:
    ret, image = cap.read()
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    image_small = cv2.resize(gray, (WIDTH, HEIGHT))
    arr = model.predict(np.asarray(image_small).reshape(1,WIDTH,HEIGHT,1))
    accel = int(arr[0][0] * 128)
    steer = int(arr[0][1] * 128)
    command = f"a{accel}xs{steer}x"
    print(f"current value {command}")
    ser.write(f"a{command}x".encode())
    count += 1
    if (count % 20 == 0):
        print(count)
        ser.write(f"px".encode())
        line = ser.readline().decode('ascii')
        file_name = "./serve/" + str(int(time.time() * 1000))
        cv2.imwrite(file_name + ".jpg", gray)
        print(line)
