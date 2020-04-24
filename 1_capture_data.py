#pip install pyserial
import cv2
import time
import serial
import os
# arduino_port = '/dev/cu.usbmodem14301'
# video_url = 'http://192.168.1.176:8080/video'
X_TOL_MIN = 15
arduino_port = '/dev/cu.wchusbserial1440'
arduino_port_2 = '/dev/cu.wchusbserial1430'
video_url = 'http://192.168.43.1:8080/video'

try:
    ser = serial.Serial(arduino_port, 57600)
except:
    ser = serial.Serial(arduino_port_2, 57600)
cap = cv2.VideoCapture(video_url)
model = load_model('models/model1.h5')

cap = cv2.VideoCapture(video_url)


def mkdir(dir):
    try:
        # Create target Directory
        os.mkdir(dir)
        print(f"Directory {dir} Created ") 
    except FileExistsError:
        print(f"Directories {dir} exists")


def read_serial():
    ser.write("px\n".encode());
    line = ser.readline().decode('ascii')
    return line

# Create the required directories if they don't exist
mkdir("./data")
mkdir("./model")
mkdir("./serve")

print("Get initial Serial")
line = read_serial()
print(line)
file_name = "./data/" + str(int(time.time() * 1000))
ret, frame = cap.read() #warm up camera

# Need a calibarion function
start_time = time.time()
val_count = 0;
xval_mid = 0
yval_mid = 0
line = ""

while (time.time() - start_time < 2):
    line = read_serial()
    if line[0] == 's':
        val = line.split(',')
        val_count += 1
        xval_mid += int(val[2])
        yval_mid += int(val[3])

if (val_count > 0):
    xval_mid = xval_mid/val_count
    yval_mid = yval_mid/val_count

print(line, xval_mid, yval_mid, val_count)

prev_ts = 0
count = 0
start_time = time.time()

print("Starting data collection\n")

limit = 100
prev_ts = 0;
count = 0;
start_time = time.time()

while True:
    binary_val = None
    if count == 0:
        start_time = time.time()

    if count == 100:
        print( f"fps {count /(time.time() - start_time)}")
        count = 0
        start_time = time.time()     

    line = read_serial()
    val = line.split(',')
    if val[0] == 's':
        forward_speed = float(val[2]);
        if (forward_speed > xval_mid + X_TOL_MIN):
            file_name = "./data/" + str(int(time.time() * 1000))
            ret, frame = cap.read()
            cv2.imwrite(file_name + ".jpg", frame)
            ctrl_file = open(file_name + ".txt","w") 
            ctrl_file.write(line)
            ctrl_file.close()
            prev_ts = int(val[1])
            count+=1