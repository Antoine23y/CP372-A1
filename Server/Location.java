public class Location {
    private final int x;
    private final int y;

    public Location(int x, int y){
        this.x = x;
        this.y = y;

    }
    public int getX() {
        return x;
    }
    public int getY(){
        return y;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Location other = (Location) obj;
        return x == other.x && y == other.y;
    }
    @Override
    public int hashCode() {
        return 31 * x + y ;
    }
    @Override
    public String toString() {
        return x + " " + y;
    }
}