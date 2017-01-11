class PersonInformation {

    String[] personInfo;

    public native void personSearch(String personNumber) 
        throws IllegalArgumentException;

    private void setInformation(String firstName, String lastName, 
            String adress, String zipCode, String city, String county) {

        personInfo = new String[6];

        personInfo[0] = firstName;
        personInfo[1] = lastName;
        personInfo[2] = adress;
        personInfo[3] = zipCode;
        personInfo[4] = city;
        personInfo[5] = county;
    }

    public String[] getInformation() {
        return personInfo;
    }

    static {
        System.loadLibrary("PersonInfo");
    }
}

