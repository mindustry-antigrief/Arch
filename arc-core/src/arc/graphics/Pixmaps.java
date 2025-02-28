package arc.graphics;

import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

import java.nio.*;
import java.util.*;

/** Various pixmap utilities. */
public class Pixmaps{
    private static final int[] offsets = {1, 0, 1, 1, 0, 1, -1, 1, -1, 0, -1, -1, 0, -1, 1, -1};
    private static Pixmap drawPixmap;
    private static IntSeq tmpArray = new IntSeq();

    public static Pixmap noise(int w, int h){
        Pixmap out = new Pixmap(w, h);
        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                out.set(x, y, Tmp.c1.rand());
            }
        }
        return out;
    }

    public static Texture noiseTex(int w, int h){
        Pixmap p = noise(w, h);
        Texture tex = new Texture(p);
        tex.setWrap(TextureWrap.repeat);
        p.dispose();
        return tex;
    }

    public static void flip(Pixmap pixmap){
        ByteBuffer pixels = pixmap.pixels;
        int numBytes = pixmap.width * pixmap.height * 4;
        byte[] lines = new byte[numBytes];
        int numBytesPerLine = pixmap.width * 4;
        for(int i = 0; i < pixmap.height; i++){
            pixels.position((pixmap.height - i - 1) * numBytesPerLine);
            pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
        }
        pixels.clear();
        pixels.put(lines);
    }

    public static Pixmap blend(PixmapRegion source, PixmapRegion over, float alpha){
        Pixmap out = new Pixmap(source.width, source.height);

        for(int y = 0; y < source.height; y++){
            for(int x = 0; x < source.width; x++){
                int c1 = source.getRaw(x, y);
                int c2 = over.getRaw(x, y);

                int val = Tmp.c1.set(c1).lerp(Tmp.c2.set(c2), alpha).rgba();
                out.setRaw(x, y, val);
            }
        }

        return out;
    }

    public static Pixmap median(Pixmap input, int radius, double percentile){
        return median(input, radius, percentile, tmpArray);
    }

    public static Pixmap median(Pixmap input, int radius, double percentile, IntSeq tmp){
        Pixmap pixmap = new Pixmap(input.width, input.height);
        input.each((x, y) -> {
            tmp.clear();
            Geometry.circle(x, y, pixmap.width, pixmap.height, radius, (cx, cy) -> tmp.add(input.get(cx, cy)));
            tmp.sort();
            pixmap.setRaw(x, y, tmp.get(Mathf.clamp((int)(tmp.size * percentile), 0, tmp.size - 1)));
        });
        return pixmap;
    }

    public static Pixmap scale(Pixmap pixmap, int width, int height, boolean filter){
        Pixmap dest = new Pixmap(width, height);
        dest.draw(pixmap, 0, 0, pixmap.width, pixmap.height, 0, 0, width, height, filter);
        return dest;
    }

    public static Pixmap scale(Pixmap input, float scale){
        return scale(input, scale, scale);
    }

    public static Pixmap scale(Pixmap input, float scalex, float scaley){
        Pixmap pixmap = new Pixmap((int)(input.width * scalex), (int)(input.height * scaley));
        for(int y = 0; y < pixmap.height; y++){
            for(int x = 0; x < pixmap.width; x++){
                pixmap.setRaw(x, y, input.getRaw((int)(x / scalex), (int)(y / scaley)));
            }
        }
        return pixmap;
    }

    public static Pixmap outline(PixmapRegion region, Color color, int radius){
        int outlineColor = color.rgba8888();
        Pixmap out = region.crop();
        for(int y = 0; y < region.height; y++){
            for(int x = 0; x < region.width; x++){

                if(region.getA(x, y) < 255){
                    boolean found = false;
                    outer:
                    for(int rx = -radius; rx <= radius; rx++){
                        for(int ry = -radius; ry <= radius; ry++){
                            if(Structs.inBounds(rx + x, ry + y, region.width, region.height) && (rx*rx + ry*ry <= radius*radius) && region.getA(rx + x, ry + y) != 0){
                                found = true;
                                break outer;
                            }
                        }
                    }
                    if(found){
                        out.set(x, y, outlineColor);
                    }
                }
            }
        }
        return out;
    }

    /** Outlines the input pixmap by 1 pixel. */
    public static Pixmap outline(Pixmap input, Color color){
        Pixmap pixmap = input.copy();
        int col = color.rgba();

        for(int y = 0; y < pixmap.height; y++){
            for(int x = 0; x < pixmap.width; x++){
                if(input.empty(x, y) &&
                ((!input.empty(x, y + 1) && y < pixmap.height - 1) || (!input.empty(x, y - 1) && y > 0) || (!input.empty(x - 1, y) && x > 0) || (!input.empty(x + 1, y) && x < pixmap.width - 1)))
                    pixmap.set(x, y, col);
            }
        }
        return pixmap;
    }

    public static Pixmap zoom(Pixmap input, int scale){
        Pixmap pixmap = new Pixmap(input.width, input.height);
        for(int x = 0; x < pixmap.width; x++){
            for(int y = 0; y < pixmap.height; y++){
                pixmap.set(x, y, input.get(x / scale + pixmap.width / 2 / scale, y / scale + pixmap.height / 2 / scale));
            }
        }
        return pixmap;
    }

    public static Pixmap resize(Pixmap input, int width, int height){
        Pixmap pixmap = new Pixmap(width, height);
        pixmap.draw(input, width / 2 - input.width / 2, height / 2 - input.height / 2);

        return pixmap;
    }

    public static Pixmap resize(Pixmap input, int width, int height, int backgroundColor){
        Pixmap pixmap = new Pixmap(width, height);
        pixmap.fill(backgroundColor);
        pixmap.draw(input, width / 2 - input.width / 2, height / 2 - input.height / 2);

        return pixmap;
    }

    public static Pixmap crop(Pixmap input, int x, int y, int width, int height){
        if(input.isDisposed()) throw new IllegalStateException("input is disposed.");
        Pixmap pixmap = new Pixmap(width, height);
        pixmap.draw(input, 0, 0, x, y, width, height);
        return pixmap;
    }

    public static Pixmap rotate(Pixmap input, float angle){
        Vec2 vector = new Vec2();
        Pixmap pixmap = new Pixmap(input.height, input.width);

        for(int y = 0; y < input.height; y++){
            for(int x = 0; x < input.width; x++){
                vector.set(x - input.width / 2f + 0.5f, y - input.height / 2f);
                vector.rotate(-angle);
                int px = (int)(vector.x + input.width / 2f + 0.01f);
                int py = (int)(vector.y + input.height / 2f + 0.01f);
                pixmap.set(px - input.width / 2 + pixmap.width / 2, py - input.height / 2 + pixmap.height / 2, input.get(x, y));
            }
        }

        return pixmap;
    }

    public static boolean empty(int i){
        return (i & 0x000000ff) == 0;
    }

    public static Pixmap huePixmap(int width, int height){
        Pixmap pixmap = new Pixmap(width, height);
        Color color = new Color(1, 1, 1, 1);

        for(int x = 0; x < width; x++){
            color.fromHsv(x / (float)width * 360f, 1f, 1);
            int rgba = color.rgba();
            for(int y = 0; y < height; y++){
                pixmap.set(x, y, rgba);
            }
        }
        return pixmap;
    }

    public static Texture hueTexture(int width, int height){
        return new Texture(huePixmap(width, height));
    }

    public static Pixmap blankPixmap(){
        Pixmap pixmap = new Pixmap(1, 1);
        pixmap.fill(Color.whiteRgba);
        return pixmap;
    }

    public static Texture blankTexture(){
        Texture texture = new Texture(new PixmapTextureData(blankPixmap(), false, true));
        texture.setWrap(TextureWrap.repeat, TextureWrap.repeat);
        return texture;
    }

    public static TextureRegion blankTextureRegion(){
        return new TextureRegion(blankTexture());
    }

    public static void drawPixel(Texture texture, int x, int y, int color){
        if(drawPixmap == null){
            drawPixmap = new Pixmap(1, 1);
        }

        drawPixmap.setRaw(0, 0, color);
        texture.draw(drawPixmap, Mathf.clamp(x, 0, texture.width - 1), Mathf.clamp(y, 0, texture.height - 1));
    }

    /**
     * Applies alpha bleeding to the target pixmap, but with only one iteration.
     * This is faster than standard iterative bleeding.
     * @return the input pixmap with its pixels modified.
     * */
    public static Pixmap bleed(Pixmap image){
        int w = image.width, h = image.height;
        ByteBuffer pixels = image.pixels;

        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                if(image.empty(x, y)){
                    int r = 0, g = 0, b = 0, count = 0;
                    int pi = (x + y*w)*4;

                    //grab for each direction
                    for(int i = 0; i < 16; i += 2){
                        int nx = x + offsets[i];
                        int ny = y + offsets[i + 1];
                        int index = (ny*w + nx)*4;
                        if(nx >= 0 && ny >= 0 && nx < w && ny < h && pixels.get(index + 3) != 0){
                            r += pixels.get(index) & 0xff;
                            g += pixels.get(index + 1) & 0xff;
                            b += pixels.get(index + 2) & 0xff;
                            count ++;
                        }
                    }

                    if(count > 0){
                        pixels.put(pi, (byte)(r / count));
                        pixels.put(pi + 1, (byte)(g / count));
                        pixels.put(pi + 2, (byte)(b / count));
                    }
                }
            }
        }
        return image;
    }

    public static void antialias(Pixmap pixmap){
        Pixmap prev = pixmap.copy();

        Color color = new Color();
        Color sum = new Color();
        Color suma = new Color();
        int[] p = new int[9];

        for(int y = 0; y < prev.height; y++){
            for(int x = 0; x < prev.width; x++){
                int A = prev.get(x - 1, y + 1),
                B = prev.get(x, y + 1),
                C = prev.get(x + 1, y + 1),
                D = prev.get(x - 1, y),
                E = prev.get(x, y),
                F = prev.get(x + 1, y),
                G = prev.get(x - 1, y - 1),
                H = prev.get(x, y - 1),
                I = prev.get(x + 1, y - 1);

                Arrays.fill(p, E);

                if(D == B && D != H && B != F) p[0] = D;
                if((D == B && D != H && B != F && E != C) || (B == F && B != D && F != H && E != A)) p[1] = B;
                if(B == F && B != D && F != H) p[2] = F;
                if((H == D && H != F && D != B && E != A) || (D == B && D != H && B != F && E != G)) p[3] = D;
                if((B == F && B != D && F != H && E != I) || (F == H && F != B && H != D && E != C)) p[5] = F;
                if(H == D && H != F && D != B) p[6] = D;
                if((F == H && F != B && H != D && E != G) || (H == D && H != F && D != B && E != I)) p[7] = H;
                if(F == H && F != B && H != D) p[8] = F;

                suma.set(0);

                for(int val : p){
                    color.rgba8888(val);
                    color.premultiplyAlpha();
                    suma.r += color.r;
                    suma.g += color.g;
                    suma.b += color.b;
                    suma.a += color.a;
                }

                float fm = suma.a <= 0.001f ? 0f : (1f / suma.a);
                suma.mul(fm, fm, fm, fm);

                float total = 0;
                sum.set(0);

                for(int val : p){
                    color.rgba8888(val);
                    float a = color.a;
                    color.lerp(suma, (1f - a));
                    sum.r += color.r;
                    sum.g += color.g;
                    sum.b += color.b;
                    sum.a += a;
                    total += 1f;
                }

                fm = (1f / total);
                sum.mul(fm, fm, fm, fm);
                pixmap.setRaw(x, y, sum.rgba8888());
                sum.set(0);
            }
        }

        prev.dispose();
    }

    /**
     * Applies alpha bleeding to the target pixmap.
     * @return the input pixmap with its pixels modified.
     * */
    public static Pixmap bleed(Pixmap image, int maxIterations){
        int total = image.width * image.height;
        ByteBuffer pixels = image.pixels;

        boolean[] data = new boolean[total];
        int[] pending = new int[total];
        int[] changing = new int[total];
        int pendingSize = 0, changingSize = 0;

        for(int i = 0; i < total; i++){
            if(pixels.get(i * 4 + 3) == 0){
                pending[pendingSize++] = i;
            }else{
                data[i] = true;
            }
        }

        int iterations = 0;
        int lastPending = -1;
        while(pendingSize > 0 && pendingSize != lastPending && iterations < maxIterations){
            lastPending = pendingSize;
            int index = 0;

            while(index < pendingSize){
                int pixelIndex = pending[index++];
                int x = pixelIndex % image.width;
                int y = pixelIndex / image.width;
                int r = 0, g = 0, b = 0;
                int count = 0;

                for(int i = 0; i < 16; i += 2){
                    int nx = x + offsets[i];
                    int ny = y + offsets[i + 1];

                    if(nx < 0 || nx >= image.width || ny < 0 || ny >= image.height) continue;

                    int currentPixelIndex = ny * image.width + nx;
                    if(data[currentPixelIndex]){
                        int si = currentPixelIndex * 4;
                        r += pixels.get(si) & 0xff;
                        g += pixels.get(si + 1) & 0xff;
                        b += pixels.get(si + 2) & 0xff;
                        count++;
                    }
                }

                if(count != 0){
                    int idx = pixelIndex * 4;
                    pixels.put(idx, (byte)(r / count));
                    pixels.put(idx + 1, (byte)(g / count));
                    pixels.put(idx + 2, (byte)(b / count));

                    index--;
                    int value = pending[index];
                    pendingSize--;
                    pending[index] = pending[pendingSize];
                    changing[changingSize] = value;
                    changingSize++;
                }
            }

            for(int i = 0; i < changingSize; i++){
                data[changing[i]] = true;
            }
            changingSize = 0;
            iterations++;
        }

        return image;
    }

}
