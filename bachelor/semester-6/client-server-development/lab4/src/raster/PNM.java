package raster;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class PNM {
    public static class UnknownFileException extends Exception {
        UnknownFileException() {
            super ("Unknown file type");
        }
    }
    protected static final int FileTypeIndex = 0;
    protected static final int WidthIndex = 1;
    protected static final int HeightIndex = 2;
    protected abstract int getMaxValueLimit();
    protected abstract int getExpectedValuesPerPixel();
    protected int width;
    protected int height;
    protected int maxValue;
    protected int valuesPerPixel;
    protected int[] data;
    public void streamWrite(OutputStream out) throws IOException {
        PrintWriter writer = new PrintWriter(out, false);
        writer.write(this.getClass().getName());
        writer.write('\0');
        writer.write(this.width);
        writer.write(this.height);
        writer.write(this.maxValue);
        writer.write(this.valuesPerPixel);
        for (int value : this.data) {
            writer.write(value);
        }
        writer.flush();
    }
    public final int getWidth() {
        return width;
    }
    public final int getHeight() {
        return height;
    }
    public final int getValuesPerPixel() {
        return valuesPerPixel;
    }
    public final int[] getData() {
        return data;
    }

    public final int getMaxValue() {
        return maxValue;
    }
    protected boolean selfVerify() {
        if (this.maxValue <= 0 || this.maxValue > this.getMaxValueLimit()) {
            return false;
        }
        if (this.valuesPerPixel != this.getExpectedValuesPerPixel()) {
            return false;
        }
        if (this.width * this.height * this.valuesPerPixel != this.data.length) {
            return false;
        }
        return true;
    }
    public void streamRead(InputStream in) throws IOException, ClassNotFoundException {
        InputStreamReader reader = new InputStreamReader(in);

        if ((this.width = reader.read()) == -1) {
            throw new ClassNotFoundException("Width reading failure");
        }
        if ((this.height = reader.read()) == -1) {
            throw new ClassNotFoundException("Height reading failure");
        }
        if ((this.maxValue = reader.read()) == -1) {
            throw new ClassNotFoundException("Max value reading failure");
        }
        if ((this.valuesPerPixel = reader.read()) == -1) {
            throw new ClassNotFoundException("Values per pixel reading failure");
        }

        this.data = new int[this.width * this.height * this.valuesPerPixel];

        for (int i = 0; i < this.data.length; i++) {
            if ((this.data[i] = reader.read()) == -1) {
                throw new ClassNotFoundException("Value reading failure");
            }
        }
        if (!this.selfVerify()) {
            throw new ClassNotFoundException("Wrong format");
        }
    }

    public static PNM getImageByPath(String filePath) throws PNM.UnknownFileException, IOException {
        String[] extensionSplit = filePath.split("\\.");
        String extension = extensionSplit[extensionSplit.length - 1];
        PNM image;
        final Path path = Paths.get(filePath);
        image = switch (extension) {
            case "pbm" -> new PBM(path);
            case "pgm" -> new PGM(path);
            case "ppm" -> new PPM(path);
            default -> throw new UnsupportedOperationException("Unsupported file type!");
        };

        return image;
    }
}
