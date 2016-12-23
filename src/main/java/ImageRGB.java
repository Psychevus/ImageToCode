import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mzaferanloo on 10/16/2016.
 */
public class ImageRGB extends Component {

    public static void main(String[] foo) {
        ArrayList<File> arrayList = getImageFile();
        for (File file : arrayList) {
            new ImageRGB(file);
        }
    }

    private void printPixelARGB(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
//        System.out.println("argb: " + alpha + ", " + red + ", " + green + ", " + blue);
    }

    private List<String> marchThroughImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        System.out.println("width, height: " + w + ", " + h);
        List<String> lines = new ArrayList<String>();
        String line;
        for (int i = 0; i < h; i++) {
            line = "";
            for (int j = 0; j < w; j++) {
//                 System.out.println("x,y: " + j + ", " + i);
                int pixel = image.getRGB(j, i);
//                System.out.println(pixel);
                if (pixel < -1)
                    line += " ";
                else
                    line += "O";

                printPixelARGB(pixel);
            }
            assert false;
            lines.add(line);
        }
        return lines;
    }

    private static ArrayList<File> getImageFile() {
        ArrayList<File> arrayList = new ArrayList<File>();

        File folder = new File("./");
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles != null ? listOfFiles : new File[0]) {
            if (file.isFile()) {
                if (file.getName().endsWith(".jpg")
                        || file.getName().endsWith(".jpeg")
                        || file.getName().endsWith(".png")) {
                    if (!file.getName().equals("image.jpg")) {
                        arrayList.add(file);
                        System.out.println("File " + file.getName());
                    }
                }
            }
        }
        return arrayList;
    }


    private ImageRGB(File originalFile) {
        try {
            // get the BufferedImage, using the ImageIO class
            BufferedImage bufferedImage = ImageIO.read(originalFile);
            int w = bufferedImage.getWidth();
            int h = bufferedImage.getHeight();
            System.out.println("width, height: " + w + ", " + h);

            int[] newInt = newSize(w, h);

            bufferedImage = toBufferedImage(bufferedImage.getScaledInstance(newInt[0], newInt[1], Image.SCALE_DEFAULT));

            float brightness = getBrightness(bufferedImage);
            BufferedImage image = thresholdImage(bufferedImage, 255 * brightness);
            List<String> lines = marchThroughImage(image);
            try {
                Path file = Paths.get(originalFile.getName().substring(0, originalFile.getName().indexOf(".")) + ".txt");
                Files.write(file, lines, Charset.forName("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private int[] newSize(int w, int h) {
        int[] newInt = new int[2];
        float division;
        division = (float) w / h;
        System.out.println(division);
        newInt[0] = 1024;
        if (w == h) {
            newInt[1] = 1024 / 3;
        } else if (w < h) {
            newInt[1] = (int) (h * division * 0.8);
        } else {
            newInt[1] = (int) (w * division * 0.8);
        }
        return newInt;
    }

    private static BufferedImage thresholdImage(BufferedImage image, float threshold) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        result.getGraphics().drawImage(image, 0, 0, null);
        WritableRaster raster = result.getRaster();
        int[] pixels = new int[image.getWidth()];
        for (int y = 0; y < image.getHeight(); y++) {
            raster.getPixels(0, y, image.getWidth(), 1, pixels);
            for (int i = 0; i < pixels.length; i++) {
                if (pixels[i] < threshold) pixels[i] = 0;
                else pixels[i] = 255;
            }
            raster.setPixels(0, y, image.getWidth(), 1, pixels);
        }
        File outputfile = new File("image.jpg");
        try {
            ImageIO.write(result, "jpg", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }


    private static float getBrightness(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        float sum = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int color = image.getRGB(x, y);

                int red = (color >>> 16) & 0xFF;
                int green = (color >>> 8) & 0xFF;
                int blue = (color) & 0xFF;

                float luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;
                sum += luminance;
            }
        }

        return (sum / (w * h));
    }
}