package Tester;

import Interface.IRecord;

import java.io.*;
import java.util.Date;

public class Osoba implements IRecord<Osoba> {
    private char[] meno;
    private char[] priezvisko;
    private Date datumNarodenia;
    private char[] UUID;

    public Osoba() {
        this.meno = new char[15];
        this.priezvisko = new char[14];
        this.datumNarodenia = new Date();
        this.UUID = new char[10];
    }

    public Osoba(char[] meno, char[] priezvisko, Date datumNarodenia, char[] UUID) {
        this.meno = meno;
        this.priezvisko = priezvisko;
        this.datumNarodenia = datumNarodenia;
        this.UUID = UUID;
    }

    public char[] getMeno() {
        return meno;
    }

    public char[] getPriezvisko() {
        return priezvisko;
    }

    public Date getDatumNarodenia() {
        return datumNarodenia;
    }

    public char[] getUUID() {
        return UUID;
    }

    @Override
    public boolean isEqual(Osoba object) {
        String uuidThis = new String(this.UUID);
        String uuidOther = new String(object.getUUID());
        return uuidThis.compareTo(uuidOther) == 0;
    }

    @Override
    public Osoba createCopy() {
        return new Osoba(this.meno.clone(), this.priezvisko.clone(), new Date(this.datumNarodenia.getTime()), this.UUID.clone());
    }

    @Override
    public Osoba fromByteArray(byte[] bytesArray) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytesArray))) {

            char[] meno = new char[15];
            for (int i = 0; i < 15; i++) {
                meno[i] = in.readChar();
            }

            char[] priezvisko = new char[14];
            for (int i = 0; i < 14; i++) {
                priezvisko[i] = in.readChar();
            }

            long dateLong = in.readLong();
            Date datum = new Date(dateLong);

            char[] uuid = new char[10];
            for (int i = 0; i < 10; i++) {
                uuid[i] = in.readChar();
            }

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

            // Write meno
            for (int i = 0; i < 15; i++) {
                char c = (i < meno.length) ? meno[i] : 0;
                out.writeChar(c);
            }

            // Write priezvisko
            for (int i = 0; i < 14; i++) {
                char c = (i < priezvisko.length) ? priezvisko[i] : 0;
                out.writeChar(c);
            }

            // Write date (as long)
            out.writeLong(datumNarodenia.getTime());

            // Write UUID
            for (int i = 0; i < 10; i++) {
                char c = (i < UUID.length) ? UUID[i] : 0;
                out.writeChar(c);
            }

            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error serializing Osoba", e);
        }
    }

    @Override
    public int getSize() {
        return Character.BYTES * 15 + Character.BYTES * 14 + Long.BYTES + Character.BYTES * 10;
    }

    @Override
    public String toString() {
        return new String(meno).trim() + " " +
                new String(priezvisko).trim() + " [" +
                new String(UUID).trim() + "]";
    }
}
