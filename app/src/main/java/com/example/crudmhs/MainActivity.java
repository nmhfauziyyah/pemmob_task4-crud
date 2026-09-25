package com.example.crudmhs;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText nrp, nama;
    private Spinner spAngkatan, spProdi;
    private TextView tvStatusLog;
    private SQLiteDatabase dbku;
    private SQLiteOpenHelper openDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nrp = findViewById(R.id.nrp);
        nama = findViewById(R.id.nama);
        spAngkatan = findViewById(R.id.spAngkatan);
        spProdi = findViewById(R.id.spProdi);
        tvStatusLog = findViewById(R.id.tvStatusLog);

        // Setup Spinner Adapters
        ArrayAdapter<CharSequence> adapterAngkatan = ArrayAdapter.createFromResource(
                this, R.array.angkatan_array, R.layout.spinner_item
        );
        adapterAngkatan.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spAngkatan.setAdapter(adapterAngkatan);

        ArrayAdapter<CharSequence> adapterProdi = ArrayAdapter.createFromResource(
                this, R.array.prodi_array, R.layout.spinner_item
        );
        adapterProdi.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spProdi.setAdapter(adapterProdi);

        // Event Listeners (Lambda)
        findViewById(R.id.btnSimpan).setOnClickListener(v -> simpan());
        findViewById(R.id.btnCari).setOnClickListener(v -> cari());
        findViewById(R.id.btnUpdate).setOnClickListener(v -> update());
        findViewById(R.id.btnHapus).setOnClickListener(v -> hapus());

        // SQLite Initialization
        openDb = new SQLiteOpenHelper(this, "db_mahasiswa", null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db) { }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
        };
        dbku = openDb.getWritableDatabase();

        // Create table if not exists with nrp, nama, angkatan, prodi
        dbku.execSQL("create table if not exists mhs(nrp TEXT, nama TEXT, angkatan TEXT, prodi TEXT);");

        // Safe upgrade if old DB exists with fewer columns
        try {
            dbku.execSQL("ALTER TABLE mhs ADD COLUMN angkatan TEXT");
        } catch (Exception ignored) { }
        try {
            dbku.execSQL("ALTER TABLE mhs ADD COLUMN prodi TEXT");
        } catch (Exception ignored) { }

        // Load last searched NRP from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("mhs_pref", MODE_PRIVATE);
        String lastNrp = prefs.getString("last_nrp", "");
        if (!lastNrp.isEmpty()) {
            nrp.setText(lastNrp);
            tvStatusLog.setText("Status: Memuat pencarian NRP terakhir (" + lastNrp + ")");
        } else {
            tvStatusLog.setText("Status: Siap menerima data");
        }
    }

    @Override
    protected void onStop() {
        if (dbku != null && dbku.isOpen()) {
            dbku.close();
        }
        super.onStop();
    }

    private void simpan() {
        String nrpVal = nrp.getText().toString().trim();
        String namaVal = nama.getText().toString().trim();
        String angkatanVal = spAngkatan.getSelectedItem() != null ? spAngkatan.getSelectedItem().toString() : "";
        String prodiVal = spProdi.getSelectedItem() != null ? spProdi.getSelectedItem().toString() : "";

        ContentValues data = new ContentValues();
        data.put("nrp", nrpVal);
        data.put("nama", namaVal);
        data.put("angkatan", angkatanVal);
        data.put("prodi", prodiVal);

        dbku.insert("mhs", null, data);
        Toast.makeText(this, "Data Tersimpan", Toast.LENGTH_LONG).show();
        tvStatusLog.setText("Status: Data NRP " + nrpVal + " Berhasil Disimpan");
    }

    private void cari() {
        String nrpInput = nrp.getText().toString().trim();
        Cursor cur = dbku.rawQuery("select * from mhs where nrp='" + nrpInput + "'", null);
        if (cur.getCount() > 0) {
            cur.moveToFirst();
            int namaIndex = cur.getColumnIndex("nama");
            int angkatanIndex = cur.getColumnIndex("angkatan");
            int prodiIndex = cur.getColumnIndex("prodi");

            String namaFound = "";
            if (namaIndex != -1) {
                namaFound = cur.getString(namaIndex);
                nama.setText(namaFound);
            }
            if (angkatanIndex != -1) {
                String savedAngkatan = cur.getString(angkatanIndex);
                setSpinnerValue(spAngkatan, savedAngkatan);
            }
            if (prodiIndex != -1) {
                String savedProdi = cur.getString(prodiIndex);
                setSpinnerValue(spProdi, savedProdi);
            }

            // Save last searched NRP to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("mhs_pref", MODE_PRIVATE);
            prefs.edit().putString("last_nrp", nrpInput).apply();

            Toast.makeText(this, "Data Ditemukan", Toast.LENGTH_LONG).show();
            tvStatusLog.setText("Status: Data NRP " + nrpInput + " Ditemukan (" + namaFound + ")");
        } else {
            Toast.makeText(this, "Data Tidak Ditemukan", Toast.LENGTH_LONG).show();
            tvStatusLog.setText("Status: Data NRP " + nrpInput + " Tidak Ditemukan");
        }
        cur.close();
    }

    private void update() {
        String nrpVal = nrp.getText().toString().trim();
        String namaVal = nama.getText().toString().trim();
        String angkatanVal = spAngkatan.getSelectedItem() != null ? spAngkatan.getSelectedItem().toString() : "";
        String prodiVal = spProdi.getSelectedItem() != null ? spProdi.getSelectedItem().toString() : "";

        ContentValues data = new ContentValues();
        data.put("nrp", nrpVal);
        data.put("nama", namaVal);
        data.put("angkatan", angkatanVal);
        data.put("prodi", prodiVal);

        dbku.update("mhs", data, "nrp='" + nrpVal + "'", null);
        Toast.makeText(this, "Data Terupdate", Toast.LENGTH_LONG).show();
        tvStatusLog.setText("Status: Data NRP " + nrpVal + " Berhasil Diperbarui");
    }

    private void hapus() {
        String nrpVal = nrp.getText().toString().trim();
        dbku.delete("mhs", "nrp='" + nrpVal + "'", null);
        Toast.makeText(this, "Data Terhapus", Toast.LENGTH_LONG).show();
        tvStatusLog.setText("Status: Data NRP " + nrpVal + " Berhasil Dihapus");
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (value.equals(adapter.getItem(i).toString())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }
    }
}
