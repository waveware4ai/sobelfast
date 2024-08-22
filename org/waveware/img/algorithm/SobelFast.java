package org.waveware.img.algorithm;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

//20210201 written by 14mhz@waveware

public class SobelFast
{
    public static void main(String args[]) throws IOException
    {
        System.out.println("[INF] start SobelFast...");
        long stt = System.currentTimeMillis();
        {
            {
                File src = new File("c:/input.png");
                File tgt = new File("c:/output1.png");
                new SobelFast(src).process().save(tgt);
            }
            
            {
                File          src = new File("c:/input.png");
                BufferedImage tgt = new SobelFast(src).process().toImage();
                ImageIO.write(tgt, "png", new File("c:/output2.png"));
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("[INF] elapsed : " + (end - stt) + " ms ..."); 
    }

    ///////////////////////////////////
    
    File          src_fle = null;
    BufferedImage src_img = null;
    private int[] tgt_rgb = null;
    
    public SobelFast()
    {
        
    }
    
    public SobelFast(File src)
    {
        this.src_fle = src;
    }
    
    public SobelFast(BufferedImage src)
    {
        this.src_img = src;
    }
    
    public void save(File tgt) throws IOException
    {
        if (tgt_rgb == null)
        {
            System.err.println("[ERR] cannot found output raster ...");
            return;
        }
        
        BufferedImage buf = RGBA2Image(tgt_rgb, w, h);
        ImageIO.write(buf, "png", tgt);
    }

    private BufferedImage toImage()
    {
        if (tgt_rgb == null)
        {
            System.err.println("[ERR] cannot found output raster ...");
            return null;
        }
        
        return RGBA2Image(tgt_rgb, w, h);
    }

    
    ///////////////////////////////////
    
    public SobelFast process() throws IOException
    {
        if (src_fle == null)
        {
            System.err.println("[ERR] cannot found input file ...");
            return this;
        }
        
        if (src_fle != null)
        {
            this.tgt_rgb = process(src_fle);
        }
        
        if (src_img != null)
        {
            this.tgt_rgb = process(src_img);
        }

        return this;
    }

    
    public int[] process(File src) throws IOException
    {
        this.src_fle = src;
        return process(ImageIO.read(src));
    }
    
    public int[] process(BufferedImage src)
    {
        if (src == null) return null;
        
        if (false)
        {
            return blackandwhite(src.getWidth(), src.getHeight(), src, val);
        }
        
        init(src.getWidth(), src.getHeight());
        if (false) { PixelGrabber grabber = new PixelGrabber(src, 0, 0, w, h, org, 0, w); try { grabber.grabPixels(); } catch(Exception _) { }} // 60msec
        if (false) { for (int i = 0; i < h; i++) for (int j = 0; j < w; j++) org[i*w + j] = gray(src.getRGB(j, i)); } // 55msec
        if (false) { src.getRGB(0, 0, w, h, org, 0, w); for (int i = 0; i < h; i++) for (int j = 0; j < w; j++) org[i*w + j] = gray(org[i*w + j]); } // 40msec
        if (true) { raster2i(src, org); for (int i = 0; i < h; i++) for (int j = 0; j < w; j++) org[i*w + j] = gray(org[i*w + j]); } // very very fast !!! 20msec
        
        int[]v = process(w, h, org, val);
        return v; //RGBA2Image(v, w, h);
    }

    int w = 0;
    int h = 0;
    int l = 0;
    int[]org = null;
    int[]val = null;
    
    public void init(int w, int h)
    {
        if (l != w*h)
        {
            this.w = w;
            this.h = h;
            this.l = w*h;
            this.org = new int[w*h];  
            this.val = new int[w*h];
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static int[] process(int w, int h, int[]org, int[]tgt)
    {// multi thread support
        if (org == null) return null;
        if (tgt == null) tgt = new int[org.length];
        
        int maxGradient = -1;
        boolean USINGGRADIENT = true;
        double scale = 0.8d;
        
        for (int i = 1; i < h - 1; i++)
        {
            int ibef = (i - 1)*w;
            int icur = (i + 0)*w;
            int inxt = (i + 1)*w;
            
            for (int j = 1; j < w - 1; j++)
            {
                int jbef = j - 1;
                int jcur = j;
                int jnxt = j + 1;
                
                int val00 = org[ibef + jbef]; 
                int val01 = org[icur + jbef]; 
                int val02 = org[inxt + jbef]; 
                
                int val10 = org[ibef + jcur]; 
                int val11 = org[icur + jcur];
                int val12 = org[inxt + jcur]; 
                
                int val20 = org[ibef + jnxt]; 
                int val21 = org[icur + jnxt]; 
                int val22 = org[inxt + jnxt]; 
        
                int gx = ((-1 * val00) + (+0 * val01) + (+1 * val02)) + ((-2 * val10) + (0 * val11) + (2 * val12)) + ((-1 * val20) + (0 * val21) + (1 * val22));
                int gy = ((-1 * val00) + (-2 * val01) + (-1 * val02)) + ((+0 * val10) + (0 * val11) + (0 * val12)) + ((+1 * val20) + (2 * val21) + (1 * val22));
                
                double gval = Math.sqrt((gx * gx) + (gy * gy));
                int g = (int) gval;
        
                if (maxGradient < g) maxGradient = g;
        
                if (USINGGRADIENT)
                {
                    tgt[i*w + j] = g;
                }
                else
                {
                    int v = g;
                    v = (int) (v * scale);
                    v = 0xff000000 | (v << 16) | (v << 8) | v;
                    tgt[i*w + j] = v;
                }
            }
        }

        scale = 255.0 / maxGradient;
        scale = Math.min(scale, 1.0f);
        if (false) System.out.println("[INF] scale : " + scale);
        
        if (USINGGRADIENT)
        for (int i = 1; i < h - 1; i++)
        for (int j = 1; j < w - 1; j++)
        {
            int v = tgt[i*w + j];
            v = (int) (v * scale);
            v = 0xff000000 | (v << 16) | (v << 8) | v;
            tgt[i*w + j] = v;
        }
        
        return tgt; //RGBA2Image(val, w, h);
    }
    
    public static int[] processresize(int oldw, int oldh, int[]src, int neow, int neoh, int[]tgt)
    {
        long stt = System.currentTimeMillis();
        int[]org = resize(oldw, oldh, src, neow, neoh, null);
        long end = System.currentTimeMillis();
        if (false) System.out.println("[INF] resize elapsed : " + (end - stt) + " ms ..."); 
        return process(neow, neoh, org, tgt);
    }
    
    public static int[]resize(int oldw, int oldh, int[]src, int neow, int neoh, int[]tgt)
    {
        if (src == null) return null;
        if (tgt == null) tgt = new int[neow*neoh];
        if (tgt.length != neow*neoh) tgt = new int[neow*neoh];
        
        for (int i = 0; i < neoh; i++) 
        for (int j = 0; j < neow; j++) 
        {
            int neox = j * oldw / neow;
            int neoy = i * oldh / neoh;
            
            int rgb = src[neoy*oldw + neox]; //src.getRGB(neox, neoy);
            tgt[i*neow + j] = rgb; //tgt.setRGB(j, i, rgb);
        }
        return tgt;
    }
    
    public static int[] blackandwhite(int w, int h, BufferedImage src, int[]val)
    {// multi thread support
        if (src == null) return null;
        if (val == null) val = new int[w * h];
        
        final int threshold = 32;
        final int blackRGB = Color.BLACK.getRGB();
        final int whiteRGB = Color.WHITE.getRGB();
        for (int y = 0; y < h; y++)
        for (int x = 0; x < w; x++)
        {
            int rgb = src.getRGB(x, y);
            int r = (rgb >> 16) & 0xff;
            int g = (rgb >>  8) & 0xff;
            int b = (rgb      ) & 0xff;
            int gray = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
            if (gray >= threshold) val[y*w + x] = whiteRGB; // setRGB(x, y) equals to [y*w + x]
            else                   val[y*w + x] = blackRGB; // setRGB(x, y) equals to [y*w + x]             
        }
        
        return val;
    }
    
    public final static int gray(int rgb)
    {// from https://en.wikipedia.org/wiki/Grayscale, calculating luminance
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >>  8) & 0xff;
        int b = (rgb      ) & 0xff;
        int gray = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
        return gray;
    }
    
    public static BufferedImage RGBA2Image(int[]data, int w, int h) // intarray_raster
    {
        try
        { // argb is default ColorModel, argb is fast than rgba @waveware !!! 
          //if (true) return RGBA2Image_use_bytearray_raster(b, w, h);
            int[]mask = new int[] { 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000};  // rbga
          //ColorModel     cm = new DirectColorModel(32, mask[3], mask[2], mask[1], mask[0]);
            SampleModel    sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, w, h, mask);
            DataBuffer     db = new DataBufferInt(data, w * h);
            WritableRaster wr = Raster.createWritableRaster(sm, db, new Point());

            return new BufferedImage(ColorModel.getRGBdefault(), wr, false, null); // argb
        }
        catch (Exception _)
        {
            _.printStackTrace();
            return null;
        }
    }
    

    public static int[]raster2i(BufferedImage src, int[]tgt)
    {// It is x3 fast, than src.getRGB(0, 0, w, h, tmp, 0, w); 
        DataBuffer buf = src.getRaster().getDataBuffer();
        
        int w = src.getWidth();
        int h = src.getHeight();
        if (tgt == null) tgt = new int[w*h];
        if (tgt.length != w*h) tgt = new int[w*h];
        
        if (false) {}
        else if (buf instanceof DataBufferByte)
        {
            int   t = src.getType();
            byte[]b = ((DataBufferByte) buf).getData();
            int []i = t == BufferedImage.TYPE_3BYTE_BGR ? b2i_3byte(b, tgt) : b2i_4byte(b, tgt);
            
            if (false)
            {// for debug
                BufferedImage img = RGBA2Image(i, src.getWidth(), src.getHeight());
                try { ImageIO.write(img, "png", new File("w:/3.png")); } catch (Exception _) {}
            }
            
            return i;
        }
        else if (buf instanceof DataBufferDouble)
        {
            double[] a = ((DataBufferDouble) buf).getData();
        }
        else if (buf instanceof DataBufferFloat)
        {
            float[] a = ((DataBufferFloat) buf).getData();
        }
        else if (buf instanceof DataBufferInt)
        {
            int[] a = ((DataBufferInt) buf).getData();
        }
        else if (buf instanceof DataBufferShort)
        {
            short[] a = ((DataBufferShort) buf).getData();
        }
        else if (buf instanceof DataBufferUShort)
        {
            short[] a = ((DataBufferUShort) buf).getData();
        }
        
        System.err.println("[ERR] must be has implementation ...");
        
        return null;
    }

    public static int[]b2i_3byte(byte[]s, int[]t) // TYPE_3BYTE_BGR, to TYPE_INT_ARGB
    {
        if (t == null) t = new int[s.length/3];
        if (s.length != t.length*3) return null; //throw new Exception ("b2i 3b to int size mismatch");
        
        for(int n = 0, p = 0; n < t.length; n++, p+=3) 
        {
            int b0 = 0xff000000;              // a
            int b1 = (s[2 + p] & 0xff) << 16; // r
            int b2 = (s[1 + p] & 0xff) << 8;  // g
            int b3 = (s[0 + p] & 0xff) << 0;  // b
            t[n] = b0 | b1 | b2 | b3;
        }
        return t;
    }
    
    public static int[]b2i_4byte(byte[]s, int[]t) // TYPE_4BYTE_ABGR, to TYPE_INT_ARGB
    {
        if (t == null) t = new int[s.length/4];
        if (s.length != t.length*4) return null; //throw new Exception ("b2i 3b to int size mismatch");
        
        for(int n = 0, p = 0; n < t.length; n++, p+=4) 
        {
            int b0 = (s[0 + p] & 0xff) << 24; // a
            int b1 = (s[3 + p] & 0xff) << 16; // r
            int b2 = (s[2 + p] & 0xff) << 8;  // g
            int b3 = (s[1 + p] & 0xff) << 0;  // b
            t[n] = b0 | b1 | b2 | b3;
        }
        return t;
    }
    
}
