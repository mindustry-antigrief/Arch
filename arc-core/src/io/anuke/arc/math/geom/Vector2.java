package io.anuke.arc.math.geom;

import io.anuke.arc.math.Interpolation;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.math.Matrix3;
import io.anuke.arc.util.ArcRuntimeException;
import io.anuke.arc.util.Time;

import java.io.Serializable;

/**
 * Encapsulates a 2D vector. Allows chaining methods by returning a reference to itself
 * @author badlogicgames@gmail.com
 */
public class Vector2 implements Serializable, Vector<Vector2>, Position{
    private static final long serialVersionUID = 913902788239530931L;
    public static final Vector2 X = new Vector2(1, 0), Y = new Vector2(0, 1), ZERO = new Vector2(0, 0);

    /** the x-component of this vector **/
    public float x;
    /** the y-component of this vector **/
    public float y;

    /** Constructs a new vector at (0,0) */
    public Vector2(){
    }

    /**
     * Constructs a vector with the given components
     * @param x The x-component
     * @param y The y-component
     */
    public Vector2(float x, float y){
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs a vector from the given vector
     * @param v The vector
     */
    public Vector2(Vector2 v){
        set(v);
    }

    public Vector2 trns(float angle, float amount){
        return set(amount, 0).rotate(angle);
    }

    public Vector2 trns(float angle, float x, float y){
        return set(x, y).rotate(angle);
    }

    /**Snaps this vector's coordinates to integers.*/
    public Vector2 snap(){
        return set((int)x, (int)y);
    }

    @Override
    public Vector2 cpy(){
        return new Vector2(this);
    }

    @Override
    public float len(){
        return (float)Math.sqrt(x * x + y * y);
    }

    @Override
    public float len2(){
        return x * x + y * y;
    }

    @Override
    public Vector2 set(Vector2 v){
        x = v.x;
        y = v.y;
        return this;
    }

    public Vector2 set(Position v){
        x = v.getX();
        y = v.getY();
        return this;
    }

    /**
     * Sets the components of this vector
     * @param x The x-component
     * @param y The y-component
     * @return This vector for chaining
     */
    public Vector2 set(float x, float y){
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public Vector2 sub(Vector2 v){
        x -= v.x;
        y -= v.y;
        return this;
    }

    /**
     * Substracts the other vector from this vector.
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @return This vector for chaining
     */
    public Vector2 sub(float x, float y){
        this.x -= x;
        this.y -= y;
        return this;
    }

    @Override
    public Vector2 nor(){
        float len = len();
        if(len != 0){
            x /= len;
            y /= len;
        }
        return this;
    }

    @Override
    public Vector2 add(Vector2 v){
        x += v.x;
        y += v.y;
        return this;
    }

    /**
     * Adds the given components to this vector
     * @param x The x-component
     * @param y The y-component
     * @return This vector for chaining
     */
    public Vector2 add(float x, float y){
        this.x += x;
        this.y += y;
        return this;
    }

    @Override
    public float dot(Vector2 v){
        return x * v.x + y * v.y;
    }

    public float dot(float ox, float oy){
        return x * ox + y * oy;
    }

    @Override
    public Vector2 scl(float scalar){
        x *= scalar;
        y *= scalar;
        return this;
    }

    /**
     * Multiplies this vector by a scalar
     * @return This vector for chaining
     */
    public Vector2 scl(float x, float y){
        this.x *= x;
        this.y *= y;
        return this;
    }

    @Override
    public Vector2 scl(Vector2 v){
        this.x *= v.x;
        this.y *= v.y;
        return this;
    }

    @Override
    public Vector2 mulAdd(Vector2 vec, float scalar){
        this.x += vec.x * scalar;
        this.y += vec.y * scalar;
        return this;
    }

    @Override
    public Vector2 mulAdd(Vector2 vec, Vector2 mulVec){
        this.x += vec.x * mulVec.x;
        this.y += vec.y * mulVec.y;
        return this;
    }

    @Override
    public float dst(Vector2 v){
        final float x_d = v.x - x;
        final float y_d = v.y - y;
        return (float)Math.sqrt(x_d * x_d + y_d * y_d);
    }

    /**
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @return the distance between this and the other vector
     */
    public float dst(float x, float y){
        final float x_d = x - this.x;
        final float y_d = y - this.y;
        return (float)Math.sqrt(x_d * x_d + y_d * y_d);
    }

    @Override
    public float dst2(Vector2 v){
        final float x_d = v.x - x;
        final float y_d = v.y - y;
        return x_d * x_d + y_d * y_d;
    }

    /**
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @return the squared distance between this and the other vector
     */
    public float dst2(float x, float y){
        final float x_d = x - this.x;
        final float y_d = y - this.y;
        return x_d * x_d + y_d * y_d;
    }

    @Override
    public Vector2 limit(float limit){
        return limit2(limit * limit);
    }

    @Override
    public Vector2 limit2(float limit2){
        float len2 = len2();
        if(len2 > limit2){
            return scl((float)Math.sqrt(limit2 / len2));
        }
        return this;
    }

    @Override
    public Vector2 clamp(float min, float max){
        final float len2 = len2();
        if(len2 == 0f) return this;
        float max2 = max * max;
        if(len2 > max2) return scl((float)Math.sqrt(max2 / len2));
        float min2 = min * min;
        if(len2 < min2) return scl((float)Math.sqrt(min2 / len2));
        return this;
    }

    @Override
    public Vector2 setLength(float len){
        return setLength2(len * len);
    }

    @Override
    public Vector2 setLength2(float len2){
        float oldLen2 = len2();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : scl((float)Math.sqrt(len2 / oldLen2));
    }

    /**
     * Converts this {@code Vector2} to a string in the format {@code (x,y)}.
     * @return a string representation of this object.
     */
    @Override
    public String toString(){
        return "(" + x + "," + y + ")";
    }

    /**
     * Sets this {@code Vector2} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param v the string.
     * @return this vector for chaining
     */
    public Vector2 fromString(String v){
        int s = v.indexOf(',', 1);
        if(s != -1 && v.charAt(0) == '(' && v.charAt(v.length() - 1) == ')'){
            try{
                float x = Float.parseFloat(v.substring(1, s));
                float y = Float.parseFloat(v.substring(s + 1, v.length() - 1));
                return this.set(x, y);
            }catch(NumberFormatException ex){
                // Throw a ArcRuntimeException
            }
        }
        throw new ArcRuntimeException("Malformed Vector2: " + v);
    }

    /**
     * Left-multiplies this vector by the given matrix
     * @param mat the matrix
     * @return this vector
     */
    public Vector2 mul(Matrix3 mat){
        float x = this.x * mat.val[0] + this.y * mat.val[3] + mat.val[6];
        float y = this.x * mat.val[1] + this.y * mat.val[4] + mat.val[7];
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Calculates the 2D cross product between this and the given vector.
     * @param v the other vector
     * @return the cross product
     */
    public float crs(Vector2 v){
        return this.x * v.y - this.y * v.x;
    }

    /**
     * Calculates the 2D cross product between this and the given vector.
     * @param x the x-coordinate of the other vector
     * @param y the y-coordinate of the other vector
     * @return the cross product
     */
    public float crs(float x, float y){
        return this.x * y - this.y * x;
    }

    /**
     * @return the angle in degrees of this vector (point) relative to the x-axis. Angles are towards the positive y-axis
     * (typically counter-clockwise) and between 0 and 360.
     */
    public float angle(){
        float angle = Mathf.atan2(x, y) * Mathf.radiansToDegrees;
        if(angle < 0) angle += 360;
        return angle;
    }

    /**
     * @return the angle in degrees of this vector (point) relative to the given vector. Angles are towards the positive y-axis
     * (typically counter-clockwise.) between -180 and +180
     */
    public float angle(Vector2 reference){
        return (float)Math.atan2(crs(reference), dot(reference)) * Mathf.radiansToDegrees;
    }

    /**Sets this vector to a random direction with the specified length.*/
    public Vector<Vector2> rnd(float length){
        setToRandomDirection().scl(length);
        return this;
    }

    /**
     * @return the angle in radians of this vector (point) relative to the x-axis. Angles are towards the positive y-axis.
     * (typically counter-clockwise)
     */
    public float angleRad(){
        return (float)Math.atan2(y, x);
    }

    /**
     * @return the angle in radians of this vector (point) relative to the given vector. Angles are towards the positive y-axis.
     * (typically counter-clockwise.)
     */
    public float angleRad(Vector2 reference){
        return (float)Math.atan2(crs(reference), dot(reference));
    }

    /**
     * Sets the angle of the vector in degrees relative to the x-axis, towards the positive y-axis (typically counter-clockwise).
     * @param degrees The angle in degrees to set.
     */
    public Vector2 setAngle(float degrees){
        return setAngleRad(degrees * Mathf.degreesToRadians);
    }

    /**
     * Sets the angle of the vector in radians relative to the x-axis, towards the positive y-axis (typically counter-clockwise).
     * @param radians The angle in radians to set.
     */
    public Vector2 setAngleRad(float radians){
        this.set(len(), 0f);
        this.rotateRad(radians);

        return this;
    }

    /**
     * Rotates the Vector2 by the given angle, counter-clockwise assuming the y-axis points up.
     * @param degrees the angle in degrees
     */
    public Vector2 rotate(float degrees){
        return rotateRad(degrees * Mathf.degreesToRadians);
    }

    /**
     * Rotates the Vector2 by the given angle around reference vector, counter-clockwise assuming the y-axis points up.
     * @param degrees the angle in degrees
     * @param reference center Vector2
     */
    public Vector2 rotateAround(Vector2 reference, float degrees){
        return this.sub(reference).rotate(degrees).add(reference);
    }

    /**
     * Rotates the Vector2 by the given angle, counter-clockwise assuming the y-axis points up.
     * @param radians the angle in radians
     */
    public Vector2 rotateRad(float radians){
        float cos = Mathf.cos(radians);
        float sin = Mathf.sin(radians);

        float newX = this.x * cos - this.y * sin;
        float newY = this.x * sin + this.y * cos;

        this.x = newX;
        this.y = newY;

        return this;
    }

    /**
     * Rotates the Vector2 by the given angle around reference vector, counter-clockwise assuming the y-axis points up.
     * @param radians the angle in radians
     * @param reference center Vector2
     */
    public Vector2 rotateAroundRad(Vector2 reference, float radians){
        return this.sub(reference).rotateRad(radians).add(reference);
    }

    /** Rotates the Vector2 by 90 degrees in the specified direction, where >= 0 is counter-clockwise and < 0 is clockwise. */
    public Vector2 rotate90(int dir){
        float x = this.x;
        if(dir >= 0){
            this.x = -y;
            y = x;
        }else{
            this.x = y;
            y = -x;
        }
        return this;
    }

    public Vector2 lerpPast(Vector2 target, float alpha){
        x = (x) + ((target.x - x) * alpha);
        y = (y) + ((target.y - y) * alpha);
        return this;
    }

    public Vector2 lerpDelta(float tx, float ty, float alpha){
        alpha = Mathf.clamp(alpha * Time.delta());
        final float invAlpha = 1.0f - alpha;
        this.x = (x * invAlpha) + (tx * alpha);
        this.y = (y * invAlpha) + (ty * alpha);
        return this;
    }

    public Vector2 lerpDelta(Vector2 target, float alpha){
        alpha = Mathf.clamp(alpha * Time.delta());
        final float invAlpha = 1.0f - alpha;
        this.x = (x * invAlpha) + (target.x * alpha);
        this.y = (y * invAlpha) + (target.y * alpha);
        return this;
    }

    @Override
    public Vector2 lerp(Vector2 target, float alpha){
        final float invAlpha = 1.0f - alpha;
        this.x = (x * invAlpha) + (target.x * alpha);
        this.y = (y * invAlpha) + (target.y * alpha);
        return this;
    }

    @Override
    public Vector2 interpolate(Vector2 target, float alpha, Interpolation interpolation){
        return lerp(target, interpolation.apply(alpha));
    }

    @Override
    public Vector2 setToRandomDirection(){
        float theta = Mathf.random(0f, Mathf.PI2);
        return this.set(Mathf.cos(theta), Mathf.sin(theta));
    }

    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(x);
        result = prime * result + Float.floatToIntBits(y);
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(obj == null) return false;
        if(getClass() != obj.getClass()) return false;
        Vector2 other = (Vector2)obj;
        if(Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) return false;
        return Float.floatToIntBits(y) == Float.floatToIntBits(other.y);
    }

    @Override
    public boolean epsilonEquals(Vector2 other, float epsilon){
        if(other == null) return false;
        if(Math.abs(other.x - x) > epsilon) return false;
        return !(Math.abs(other.y - y) > epsilon);
    }

    /**
     * Compares this vector with the other vector, using the supplied epsilon for fuzzy equality testing.
     * @return whether the vectors are the same.
     */
    public boolean epsilonEquals(float x, float y, float epsilon){
        if(Math.abs(x - this.x) > epsilon) return false;
        return !(Math.abs(y - this.y) > epsilon);
    }

    /**
     * Compares this vector with the other vector using Mathf.FLOAT_ROUNDING_ERROR for fuzzy equality testing
     * @param other other vector to compare
     * @return true if vector are equal, otherwise false
     */
    public boolean epsilonEquals(final Vector2 other){
        return epsilonEquals(other, Mathf.FLOAT_ROUNDING_ERROR);
    }

    /**
     * Compares this vector with the other vector using Mathf.FLOAT_ROUNDING_ERROR for fuzzy equality testing
     * @param x x component of the other vector to compare
     * @param y y component of the other vector to compare
     * @return true if vector are equal, otherwise false
     */
    public boolean epsilonEquals(float x, float y){
        return epsilonEquals(x, y, Mathf.FLOAT_ROUNDING_ERROR);
    }

    @Override
    public boolean isUnit(){
        return isUnit(0.000000001f);
    }

    @Override
    public boolean isUnit(final float margin){
        return Math.abs(len2() - 1f) < margin;
    }

    @Override
    public boolean isZero(){
        return x == 0 && y == 0;
    }

    @Override
    public boolean isZero(final float margin){
        return len2() < margin;
    }

    @Override
    public boolean isOnLine(Vector2 other){
        return Mathf.isZero(x * other.y - y * other.x);
    }

    @Override
    public boolean isOnLine(Vector2 other, float epsilon){
        return Mathf.isZero(x * other.y - y * other.x, epsilon);
    }

    @Override
    public boolean isCollinear(Vector2 other, float epsilon){
        return isOnLine(other, epsilon) && dot(other) > 0f;
    }

    @Override
    public boolean isCollinear(Vector2 other){
        return isOnLine(other) && dot(other) > 0f;
    }

    @Override
    public boolean isCollinearOpposite(Vector2 other, float epsilon){
        return isOnLine(other, epsilon) && dot(other) < 0f;
    }

    @Override
    public boolean isCollinearOpposite(Vector2 other){
        return isOnLine(other) && dot(other) < 0f;
    }

    @Override
    public boolean isPerpendicular(Vector2 vector){
        return Mathf.isZero(dot(vector));
    }

    @Override
    public boolean isPerpendicular(Vector2 vector, float epsilon){
        return Mathf.isZero(dot(vector), epsilon);
    }

    @Override
    public boolean hasSameDirection(Vector2 vector){
        return dot(vector) > 0;
    }

    @Override
    public boolean hasOppositeDirection(Vector2 vector){
        return dot(vector) < 0;
    }

    @Override
    public Vector2 setZero(){
        this.x = 0;
        this.y = 0;
        return this;
    }

    @Override
    public float getX(){
        return x;
    }

    @Override
    public float getY(){
        return y;
    }
}
