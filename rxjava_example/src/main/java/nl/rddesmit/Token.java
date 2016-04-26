package nl.rddesmit;

/**
 * Return '2' or a error on subscribe
 *
 * Created by rudies on 25-4-2016.
 */
class Token extends RandomError<Integer>{
    Token(){
        super(2);
    }
}