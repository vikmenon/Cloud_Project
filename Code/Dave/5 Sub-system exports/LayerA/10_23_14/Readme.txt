This layer reads in an input video file located in Input folder, and outputs a set of jpeg images in the Output folder. 

The images are the keyframes detected and extracted from the video. Keyframes in this context are defined as scene changes. 

To find a keyframe, every frame is correlated with its next frame in the sequence. A threshold determines if the frame should be dropped, or added to the list of subject frames. 

The extracted keyframes are saved in the format:
y_fx.JPG
where:
 y: input filename
 f: (doesn't change. means "frame")
 x: keyframe#
 JPG: JPEG file format extension


The threshold for the computation during this shipment was set to 0.2.

