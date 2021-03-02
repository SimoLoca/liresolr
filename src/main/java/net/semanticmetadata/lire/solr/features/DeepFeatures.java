package net.semanticmetadata.lire.solr.features;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;

public class DeepFeatures implements GlobalFeature {
    double[] features;

    /**
     * Funzione usata per estrarre vettore di feature dalla rispettiva immagine.
     *
     * @param imageURL
     * @throws IOException
     */
    public /*synchronized*/ void extractLireq(String imageURL) {
	// System.out.println("Sto estraendo (custom)...");
        String line;
        //synchronized (this) {
        try {
            File file = new File("/home/imo/Downloads/ivl/ivl_retrieval_tools/Python_Similaity/static/features.txt");

            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                if (!line.split(":")[0].equals(imageURL)) {
                    continue;
                } else {
                    String[] vector = line.split(":")[1].split(",");
                    this.features = new double[vector.length];
                    for (int j = 0; j < this.features.length; j++)
                        this.features[j] = Double.parseDouble(vector[j]);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //}
    }
    
    @Override
    public void extract(BufferedImage bufferedImage) {
        this.features = new double[256];
        System.out.println("Sto estraendo...");
        
        File inputFile = new File("/home/imo/Downloads/ivl/ivl_retrieval_tools/Python_Similaity/static/features.csv");
        File tempFile = new File("/home/imo/Downloads/ivl/ivl_retrieval_tools/Python_Similaity/static/myTempFile.txt");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            String lineToRemove = reader.readLine();
            System.out.println(lineToRemove);
            String[] vector = lineToRemove.split(",");
            System.out.println(Arrays.toString(vector));
            for (int i = 0; i < this.features.length; i++) 
                this.features[i] = Double.parseDouble(vector[i]);
            String currentLine;
            reader.close();
            reader = new BufferedReader(new FileReader(inputFile));
            while((currentLine = reader.readLine()) != null) {
                System.out.println(currentLine);
                if(currentLine.equals(lineToRemove)) continue;
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();

            boolean successful = tempFile.renameTo(inputFile);
            System.out.println(successful);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getFeatureName() { return "Deep Features"; }

    @Override
    public String getFieldName() { return "Deep Features"; }

    @Override
    public byte[] getByteArrayRepresentation() {
    	/*
        byte[] result = new byte[this.features.length];
        for(int i = 0; i < result.length; ++i)
            result[i] = (byte)((int)this.features[i]);
        return result;
        */
        int numDimensions = 0;
        LinkedList<Double> d = new LinkedList<>();
        double[] data = this.features;
        for (int i = 0, dataLength = data.length; i < dataLength; i++) {
            double datum = data[i];
            if (datum != 0) {
                d.add((double) i);
                d.add(datum);
                numDimensions++;
            }
        }
        d.addFirst((double) data.length);
        numDimensions = 0; // re-using a variable.
        double[] s = new double[d.size()];
        for (Double aDouble : d) {
            s[numDimensions++] = aDouble;
        }
        return SerializationUtils.toByteArray(s);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) { setByteArrayRepresentation(in, 0, in.length); }

    @Override
    public void setByteArrayRepresentation(byte[] featureData, int offset, int length) {
        double[] s = SerializationUtils.toDoubleArray(featureData, offset, length);
        double[] data = new double[(int) s[0]];
        for (int i = 1; i < s.length; i+=2) {
            data[(int) s[i]] = s[i+1];
        }
        this.features = data.clone();
    }

    @Override
    public double getDistance(LireFeature lireFeature) {
        return MetricsUtils.distL2(this.features, ((DeepFeatures)lireFeature).features);
    }

    @Override
    public double[] getFeatureVector() {
        double[] result = new double[this.features.length];
        System.arraycopy(this.features, 0, result, 0, this.features.length);
        return result;
    }
}

