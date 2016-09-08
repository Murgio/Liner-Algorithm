# Liner Algorithm
#####Transforms an image into a knit pattern.

---

## Result

Output Face with 6000 lines             |  Input Face
:-------------------------:|:-------------------------:
![output_image](https://raw.githubusercontent.com/Murgio/Liner-Algorithm/master/face_output_6000_lines.png)  |  ![input_image](https://raw.githubusercontent.com/Murgio/Liner-Algorithm/master/face.png)

---

## Demonstration

![demo_gif](https://github.com/Murgio/Liner-Algorithm/blob/master/Face_Gif.gif)

---

## Overview
The algorithm finds the darkest line between two pins. It adds this line to a new blank image and removes the same line from 
the original image. The output is an image created out of straight lines where every line goes through the whole image.

---

## Features
* Number of lines can be changed
* Prints out the individual steps so it can be copied with real strings
* Calculates the lines pretty fast
* Written in pure Java
