package raster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Integer.parseInt;

public class PBM extends PNM {
    protected static final String FileTypeString = "P1"; // we only support ASCII PNM
    private static final int FirstPixelIndex = 3;
    private static final int MinFileParts = 4;

    protected int getMaxValueLimit() {
        return 1;
    }
    protected int getExpectedValuesPerPixel() {
        return 1;
    }

    public PBM() {
        this.maxValue = getMaxValueLimit();
        this.width = 1;
        this.height = 1;
        this.data = new int[]{0};
    }
    public PBM(Path path) throws IOException, UnknownFileException {
        int dataEnd;
        String fileContents = new String(Files.readAllBytes(path));
        String[] fileParts = fileContents.split("\\W+");

        this.maxValue = getMaxValueLimit();
        this.valuesPerPixel = getExpectedValuesPerPixel();

        if (fileParts.length < PBM.MinFileParts) { // checking minimal file length
            throw new UnknownFileException();
        }
        if (!fileParts[FileTypeIndex].equals(FileTypeString)) { // checking if it's ASCII PBM
            throw new UnknownFileException();
        }

        this.width = parseInt(fileParts[PBM.WidthIndex]);
        if (this.width <= 0) { // checking if the width is valid
            throw new UnknownFileException();
        }

        this.height = parseInt(fileParts[PBM.HeightIndex]);
        if (this.height <= 0) { // checking if the height is valid
            throw new UnknownFileException();
        }

        if (fileParts[fileParts.length - 1].equals("")) {
            dataEnd = fileParts.length - 1;
        }
        else {
            dataEnd = fileParts.length;
        }

        if (this.width * this.height != dataEnd - PBM.FirstPixelIndex) { // checking dimension correctness
            throw new UnknownFileException();
        }

        this.data = new int[this.width * this.height];
        for (int i = PBM.FirstPixelIndex; i < dataEnd; i++) { // reading pixel values
            this.data[i - PBM.FirstPixelIndex] = parseInt(fileParts[i]);
            if (this.data[i - PBM.FirstPixelIndex] < 0 || this.data[i - PBM.FirstPixelIndex] > getMaxValueLimit()) {
                throw new UnknownFileException();
            }
        }
    }
}
