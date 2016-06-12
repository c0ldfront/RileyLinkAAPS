package com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpData.records;


import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.gxwtech.roundtrip2.RoundtripService.medtronic.PumpModel;

import java.util.ArrayList;

public class UnabsorbedInsulin extends Record {
    private static final String TAG = "UnabsorbedInsulin";

    class UnabsorbedInsulinRecord {
        public double amount = 0.0;
        public int age = 0;
        public UnabsorbedInsulinRecord(double amount, int age) {
            this.amount = amount;
            this.age = age;
        }
    }

    ArrayList<UnabsorbedInsulinRecord> records = new ArrayList<>();

    public UnabsorbedInsulin() {
    }

    @Override
    public boolean parseFrom(byte[] data, PumpModel model) {
        length = asUINT8(data[1]);
        if (length < 2) {
            length = 2;
        }
        if (length > data.length) {
            return false;
        }
        int numRecords = (asUINT8(data[1]) - 2) / 3;
        for (int i=0; i<numRecords; i++) {
            double amount = (double)(asUINT8(data[2 + (i * 3)])) / 40.0;
            int age = asUINT8(data[3 + (i*3)]) + (((asUINT8(data[4 + (i*3)])) & 0b110000) << 4);
            records.add(new UnabsorbedInsulinRecord(amount,age));
        }

        return true;
    }

    @Override
    public boolean readFromBundle(Bundle in) {
        float[] storedAmounts = in.getFloatArray("amounts");
        int[] storedAges = in.getIntArray("ages");
        if ((storedAmounts == null) || (storedAges == null)) {
            Log.e(TAG,"readFromBundle: failed to load from bundle: null array");
        } else if (storedAges.length != storedAmounts.length) {
            Log.e(TAG,"readFromBundle: failed to load from bundle: array size mismatch");
        } else {
            for (int i=0; i<storedAges.length; i++) {
                records.add(new UnabsorbedInsulinRecord(storedAmounts[i],storedAges[i]));
            }
        }
        return super.readFromBundle(in);
    }

    @Override
    public void writeToBundle(Bundle in) {
        // Use parallel arrays to serialize the data.  Note there is a small loss
        // of precision when going from double to float.
        float[] storedAmounts = new float[records.size()];
        int[] storedAges = new int[records.size()];
        for (int i=0; i<records.size(); i++) {
            storedAmounts[i] = (float)records.get(i).amount;
            storedAges[i] = records.get(i).age;
        }
        in.putFloatArray("amounts",storedAmounts);
        in.putIntArray("ages",storedAges);

        super.writeToBundle(in);
    }

}
