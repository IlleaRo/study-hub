package raster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Integer.parseInt;

public class PGM extends PNM {
    private static final String FileTypeString = "P2"; // we only support ASCII PNM
    private static final int MaxValueIndex = 3;
    private static final int FirstPixelIndex = 4;
    private static final int MinFileParts = 5;
    protected int getMaxValueLimit() {
        return 65535;
    }
    protected int getExpectedValuesPerPixel() {
        return 1;
    }
    public PGM() {
        this.maxValue = getMaxValueLimit();
        this.width = 1;
        this.height = 1;
        this.data = new int[]{0};
    }
    public PGM(Path path) throws IOException, UnknownFileException {
        int dataEnd;
        String fileContents = new String(Files.readAllBytes(path));
        String[] fileParts = fileContents.split("\\W+");

        this.valuesPerPixel = getExpectedValuesPerPixel();

        if (fileParts.length < PGM.MinFileParts) { // checking minimal file length
            throw new UnknownFileException();
        }
        if (!fileParts[FileTypeIndex].equals(FileTypeString)) { // checking if it's ASCII PBM
            throw new UnknownFileException();
        }

        this.width = parseInt(fileParts[PGM.WidthIndex]);
        if (this.width <= 0) { // checking if the width is valid
            throw new UnknownFileException();
        }

        this.height = parseInt(fileParts[PGM.HeightIndex]);
        if (this.height <= 0) { // checking if the height is valid
            throw new UnknownFileException();
        }

        this.maxValue = parseInt(fileParts[PGM.MaxValueIndex]);
        if (this.maxValue <= 0 || maxValue >= getMaxValueLimit()) { // checking if the height is valid
            throw new UnknownFileException();
        }

        if (fileParts[fileParts.length - 1].equals("")) {
            dataEnd = fileParts.length - 1;
        }
        else {
            dataEnd = fileParts.length;
        }

        if (this.width * this.height * this.valuesPerPixel!= dataEnd - PGM.FirstPixelIndex) { // checking dimension correctness
            throw new UnknownFileException();
        }

        this.data = new int[this.width * this.height * this.valuesPerPixel];
        for (int i = PGM.FirstPixelIndex; i < dataEnd; i++) { // reading pixel values
            this.data[i - PGM.FirstPixelIndex] = parseInt(fileParts[i]);
            if (this.data[i - PGM.FirstPixelIndex] < 0 || this.data[i - PGM.FirstPixelIndex] > getMaxValueLimit()) {
                throw new UnknownFileException();
            }
        }
    }
}
