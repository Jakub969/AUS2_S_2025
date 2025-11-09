package Tester;

import Interface.IRecord;

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
        return null;
    }

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }

    @Override
    public int getSize() {
        return Character.BYTES * 15 + Character.BYTES * 14 + Long.BYTES + Character.BYTES * 10;
    }
}
