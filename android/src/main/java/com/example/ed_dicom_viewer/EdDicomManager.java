package com.example.ed_dicom_viewer;

import android.graphics.Bitmap;

import com.imebra.CodecFactory;
import com.imebra.ColorTransformsFactory;
import com.imebra.DataSet;
import com.imebra.DrawBitmap;
import com.imebra.Image;
import com.imebra.Memory;
import com.imebra.PatientName;
import com.imebra.PipeStream;
import com.imebra.StreamReader;
import com.imebra.TagId;
import com.imebra.TransformsChain;
import com.imebra.VOILUT;
import com.imebra.drawBitmapType_t;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
public class EdDicomManager {
    ResponseModel readDicomFile(byte[] fileBytes) {
        try {
            CodecFactory.setMaximumImageSize(1000, 4000);
            InputStream stream = new ByteArrayInputStream(fileBytes);
            PipeStream imebraPipe = new PipeStream(32000);
            Thread pushThread = new Thread(new ImebraPipeStream(imebraPipe, stream));
            pushThread.start();
            DataSet loadDataSet = CodecFactory.load(new StreamReader(imebraPipe.getStreamInput()));
            Image dicomFile = loadDataSet.getImageApplyModalityTransform(0);
            TransformsChain chain = new TransformsChain();
            if (ColorTransformsFactory.isMonochrome(dicomFile.getColorSpace())) {
                VOILUT voilut = new VOILUT(VOILUT.getOptimalVOI(dicomFile, 0, 0, dicomFile.getWidth(), dicomFile.getHeight()));
                chain.addTransform(voilut);
            }
            DrawBitmap drawBitmap = new DrawBitmap(chain);
            Memory memory = drawBitmap.getBitmap(dicomFile, drawBitmapType_t.drawBitmapRGBA, 4);
            Bitmap renderBitmap = Bitmap.createBitmap((int) dicomFile.getWidth(), (int) dicomFile.getHeight(), Bitmap.Config.ARGB_8888);
            byte[] memoryByte = new byte[(int) memory.size()];
            memory.data(memoryByte);
            ByteBuffer byteBuffer = ByteBuffer.wrap(memoryByte);
            renderBitmap.copyPixelsFromBuffer(byteBuffer);
            String patientName = loadDataSet
                    .getPatientName(new TagId(0x10, 0x10), 0, new PatientName("", "", ""))
                    .getAlphabeticRepresentation();
            String patientGender = loadDataSet.getString(new TagId(0x10, 0x40), 0, "");
            String patientID = loadDataSet.getString(new TagId(0x10, 0x20), 0, "");
            String patientAge = loadDataSet.getString(new TagId(0x10, 0x1010), 0, "");
            String patientBirthDate = loadDataSet.getString(new TagId(0x10, 0x30), 0, "");
            String patientWeight = loadDataSet.getString(new TagId(0x10, 0x1030), 0, "");
            String patientHeight = loadDataSet.getString(new TagId(0x10, 0x1020), 0, "");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


            renderBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            renderBitmap.recycle();
            return new ResponseModel(byteArray, patientName, patientGender, patientID, patientAge, patientBirthDate, patientWeight, patientHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
