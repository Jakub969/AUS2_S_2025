package Tester;

import Interface.IRecord;

import java.io.*;
import java.util.Date;

public class Osoba implements IRecord<Osoba> {
    private String meno;
    private final int MAX_MENO_LENGTH = 15;
    private String priezvisko;
    private final int MAX_PRIEZVISKO_LENGTH = 14;
    private Date datumNarodenia;
    private String UUID;
    private final int UUID_LENGTH = 10;

    public Osoba() {
        this.meno = "";
        this.priezvisko = "";
        this.datumNarodenia = new Date(0);
        this.UUID = "";
    }

    public Osoba(String meno, String priezvisko, Date datumNarodenia, String UUID) {
        this.meno = meno;
        this.priezvisko = priezvisko;
        this.datumNarodenia = datumNarodenia;
        this.UUID = UUID;
    }

    public String getMeno() {
        return this.meno;
    }

    public String getPriezvisko() {
        return this.priezvisko;
    }

    public Date getDatumNarodenia() {
        return this.datumNarodenia;
    }

    public String getUUID() {
        return this.UUID;
    }

    @Override
    public boolean isEqual(Osoba object) {
        return this.UUID.equals(object.UUID);
    }

    @Override
    public Osoba createCopy() {
        return new Osoba(this.meno, this.priezvisko, new Date(this.datumNarodenia.getTime()), this.UUID);
    }

    @Override
    public Osoba fromByteArray(byte[] bytesArray) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytesArray))) {
            int menoLen = in.readInt();
            String meno = this.readFixedString(in, this.MAX_MENO_LENGTH).substring(0, menoLen);

            int priezLen = in.readInt();
            String priezvisko = this.readFixedString(in, this.MAX_PRIEZVISKO_LENGTH).substring(0, priezLen);

            long dateLong = in.readLong();
            Date datum = new Date(dateLong);

            int uuidLen = in.readInt();
            String uuid = this.readFixedString(in, this.UUID_LENGTH).substring(0, uuidLen);

            this.meno = meno;
            this.priezvisko = priezvisko;
            this.datumNarodenia = datum;
            this.UUID = uuid;

            return this;

        } catch (IOException e) {
            throw new RuntimeException("Error deserializing Osoba", e);
        }
    }

    @Override
    public byte[] toByteArray() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(baos)) {
            // Meno
            out.writeInt(Math.min(this.meno.length(), this.MAX_MENO_LENGTH));
            this.writeFixedString(out, this.meno, this.MAX_MENO_LENGTH);

            // Priezvisko
            out.writeInt(Math.min(this.priezvisko.length(), this.MAX_PRIEZVISKO_LENGTH));
            this.writeFixedString(out, this.priezvisko, this.MAX_PRIEZVISKO_LENGTH);

            // Date
            out.writeLong(this.datumNarodenia.getTime());

            // UUID
            out.writeInt(Math.min(this.UUID.length(), this.UUID_LENGTH));
            this.writeFixedString(out, this.UUID, this.UUID_LENGTH);

            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error serializing Osoba", e);
        }
    }
    private void writeFixedString(DataOutputStream out, String value, int maxLen) throws IOException {
        for (int i = 0; i < maxLen; i++) {
            char c = (i < value.length()) ? value.charAt(i) : 0;
            out.writeChar(c);
        }
    }

    private String readFixedString(DataInputStream in, int maxLen) throws IOException {
        char[] chars = new char[maxLen];
        for (int i = 0; i < maxLen; i++) {
            chars[i] = in.readChar();
        }
        return new String(chars).replace("\u0000", "");
    }

    @Override
    public int getSize() {
        return Integer.BYTES * 3 + (Character.BYTES * (this.MAX_MENO_LENGTH + this.MAX_PRIEZVISKO_LENGTH + this.UUID_LENGTH)) + Long.BYTES;
    }

    @Override
    public String toString() {
        return this.meno + " " + this.priezvisko + " " + this.datumNarodenia + " " + this.UUID;
    }
}
