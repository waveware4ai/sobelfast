This code is a fast sobel algorithm implementation.

example1 - file input, file output
File src = new File("c:/input.png");
File tgt = new File("c:/output1.png");
new SobelFast(src).process().save(tgt);

example2 - file input, image output
File          src = new File("c:/input.png");
BufferedImage tgt = new SobelFast(src).process().toImage();
ImageIO.write(tgt, "png", new File("c:/output2.png"));
