import cv2
import numpy as np
from PIL import Image
# import Image
import glob
import math
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn import preprocessing

from keras.preprocessing.image import ImageDataGenerator
from keras import Sequential
from keras.layers import Input, Cropping2D, Dense
from keras.layers import Dropout
from keras.layers import Convolution2D, Flatten
from keras.layers import BatchNormalization
from keras import Model

WIDTH = 75
HEIGHT = 100

camera_records = glob.glob('data/*.jpg')

imgs = []
steer  = []
accel = []

for img_name in camera_records:
    image = Image.open(img_name)
    new_image = image.resize((WIDTH, HEIGHT))
    new_image.save(img_name[:-4] + ".jpg")

    file1 = open("data/" + img_name[5:-4] + ".txt","r") 
    txt = file1.readline().split(',')
    if (txt[0] == 's'):
        accel.append(float(txt[2]) / 128)
        steer.append(float(txt[3]) / 128)  
        imgs.append(img_name[5:-4] + ".jpg")

df = pd.DataFrame()
df['file_name'] = imgs
df['steer'] = steer
df['accel'] = accel

df_train, df_val = train_test_split(df, test_size=0.2)

image_loader = ImageDataGenerator()

train_generator = image_loader.flow_from_dataframe(
    dataframe=df_train, directory="data", x_col='file_name', y_col=['steer', 'accel'], 
    target_size=(WIDTH, HEIGHT), color_mode='grayscale', class_mode='raw', batch_size=32)

val_generator = image_loader.flow_from_dataframe(
    dataframe=df_val, directory="data", x_col='file_name', y_col=['steer', 'accel'], 
    target_size=(WIDTH, HEIGHT), color_mode='grayscale', class_mode='raw', batch_size=32)


def build_CNN(num_outputs, input_shape=(WIDTH, HEIGHT, 1), roi_crop=(0, 0)):
    drop = 0.1
    img_in = Input(shape=input_shape, name='img_in')
    x = Cropping2D(cropping=(roi_crop, (0,0)))(img_in) #trim pixels off top and bottom
    x = BatchNormalization()(x)
    x = Convolution2D(24, (5,5), strides=(2,2), activation='relu', name="conv2d_1")(x)
    x = Dropout(drop)(x)
    x = Convolution2D(32, (5,5), strides=(2,2), activation='relu', name="conv2d_2")(x)
    x = Dropout(drop)(x)
    x = Convolution2D(64, (5,5), strides=(2,2), activation='relu', name="conv2d_3")(x)
    x = Dropout(drop)(x)
    x = Convolution2D(64, (3,3), strides=(1,1), activation='relu', name="conv2d_4")(x)
    x = Dropout(drop)(x)
    x = Convolution2D(64, (3,3), strides=(1,1), activation='relu', name="conv2d_5")(x)
    x = Dropout(drop)(x)
    
    x = Flatten(name='flattened')(x)
    x = Dense(100, activation='relu')(x)
    x = Dropout(drop)(x)
    x = Dense(50, activation='relu')(x)
    x = Dropout(drop)(x)

    outputs = Dense(num_outputs, activation='linear', name='n_outputs')(x)
        
    model = Model(inputs=[img_in], outputs=outputs)
    
    return model

model = build_CNN(num_outputs = 2)
model.compile(optimizer = 'adam', loss = 'binary_crossentropy')

hist = model.fit_generator(generator=train_generator,
                    steps_per_epoch=100,
                    validation_data=val_generator,
                    validation_steps=20,
                    epochs=5)

model.save('models/model1.h5')

# Test the model
image = Image.open(camera_records[0]).convert('L') #convert to grayscale
arr = model.predict(np.asarray(image).reshape(1,WIDTH,HEIGHT,1))

file1 = open("data/" + img_name[5:-4] + ".txt","r") 
txt = file1.readline().split(',')

print(f"prediction results: {arr[0][0] * 128} {arr[0][1] * 128}")
print(f"original results: {txt[2]} {txt[3]}")