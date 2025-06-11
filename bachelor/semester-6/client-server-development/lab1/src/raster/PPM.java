package raster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Integer.parseInt;

public class PPM extends PNM {
    private static final String FileTypeString = "P3"; // we only support ASCII PNM
    private static final int MaxValueIndex = 3;
    private static final int FirstPixelIndex = 4;
    private static final int MinFileParts = 7;
    protected int getMaxValueLimit() {
        return 65535;
    }
    protected int getExpectedValuesPerPixel() {
        return 3;
    }
    public PPM() {
        this.maxValue = getMaxValueLimit();
        this.width = 1;
        this.height = 1;
        this.data = new int[]{0, 0, 0};
    }
    public PPM(Path path) throws IOException, UnknownFileException {
        int dataEnd;
        String fileContents = new String(Files.readAllBytes(path));
        String[] fileParts = fileContents.split("\\W+");

        this.valuesPerPixel = getExpectedValuesPerPixel();

        if (fileParts.length < PPM.MinFileParts) { // checking minimal file length
            throw new UnknownFileException();
        }
        if (!fileParts[PNM.FileTypeIndex].equals(FileTypeString)) { // checking if it's ASCII PBM
            throw new UnknownFileException();
        }

        this.width = parseInt(fileParts[PNM.WidthIndex]);
        if (this.width <= 0) { // checking if the width is valid
            throw new UnknownFileException();
        }

        this.height = parseInt(fileParts[PNM.HeightIndex]);
        if (this.height <= 0) { // checking if the height is valid
            throw new UnknownFileException();
        }

        this.maxValue = parseInt(fileParts[PPM.MaxValueIndex]);
        if (this.maxValue <= 0 || maxValue >= getMaxValueLimit()) { // checking if the height is valid
            throw new UnknownFileException();
        }

        if (fileParts[fileParts.length - 1].equals("")) {
            dataEnd = fileParts.length - 1;
        }
        else {
            dataEnd = fileParts.length;
        }

        if (this.width * this.height * this.valuesPerPixel != dataEnd - PPM.FirstPixelIndex) { // checking dimension correctness
            throw new UnknownFileException();
        }

        this.data = new int[this.width * this.height * this.valuesPerPixel];
        for (int i = PPM.FirstPixelIndex; i < dataEnd; i++) { // reading pixel values
            this.data[i - PPM.FirstPixelIndex] = parseInt(fileParts[i]);
            if (this.data[i - PPM.FirstPixelIndex] < 0 || this.data[i - PPM.FirstPixelIndex] > getMaxValueLimit()) {
                throw new UnknownFileException();
            }
        }
    }
}
