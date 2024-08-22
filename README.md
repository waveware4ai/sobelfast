
# sobelfast java
This code is a fast sobel algorithm implementation. When processing a 1024x768 image, even native-c sobel algorithm takes about 100ms, whereas using sobelfast-java took about 60ms. 
This library can be conveniently used to convert/process images in real time. Also, it can be used freely as long as you do not change the package name.

# example1 - file input, file output
```
File src = new File("c:/input.png");
File tgt = new File("c:/output1.png");
new SobelFast(src).process().save(tgt);
```

# example2 - file input, image output
```
File          src = new File("c:/input.png");
BufferedImage tgt = new SobelFast(src).process().toImage();
ImageIO.write(tgt, "png", new File("c:/output2.png"));
```
# example3 - image input, image output
```
BufferedImage src = ImageIO.read(new File("c:/input.jpg"));
BufferedImage tgt = new SobelFast(src).process().toImage();
ImageIO.write(tgt, "png", new File("c:/output2.jpg"));
```
