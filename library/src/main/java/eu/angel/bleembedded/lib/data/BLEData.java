/**
 * Copyright (c) 2016 University of Brescia, Alessandra Flammini and Angelo Vezzoli, All rights reserved.
 *
 * @author  Angelo Vezzoli
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package eu.angel.bleembedded.lib.data;

/**
 * Class of the data handled by the library
 */
public class BLEData {



    protected int data_type;
    protected float value;
    protected Sensor sensor;

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value=value;
    }

    public int getData_type() {
        return data_type;
    }

    public String getSensor_type() {
        return sensor.sensor_type;
    }

    public String getSensorDescription() {
        return sensor.description;
    }

    public String getSensorAccuracy() {
        return sensor.accuracy;
    }

    public String getSensorDrift() {
        return sensor.drift;
    }

    public String getSensorMeasurementRange() {
        return sensor.measurementRange;
    }

    public String getSensorMeasurementFrequency() {
        return sensor.measurementFrequency;
    }

    public String getSensorMeasurementLatency() {
        return sensor.measurementLatency;
    }

    public String getSensorPrecision() {
        return sensor.precision;
    }

    public String getSensorResolution() {
        return sensor.resolution;
    }

    public String getSensorResponseTime() {
        return sensor.responseTime;
    }

    public String getSensorSelectivity() {
        return sensor.selectivity;
    }

    public String getSensorDetectionLimit() {
        return sensor.detectionLimit;
    }

    public String getSensorCondition() {
        return sensor.condition;
    }

    public String getSensorUnit() {
        return sensor.unit;
    }


    /**
     * Constructor
     * @param data_type the type of the {@link BLEData} (the available type is defined by the library)
     */
    protected BLEData(int data_type){
        this.data_type=data_type;
    }

    /**
     * Clones the {@link BLEData}
     */
    public BLEData clone(){
        BLEData bleData=new BLEData(this.data_type);
        bleData.value=this.value;
        if (this.sensor!=null)
            bleData.sensor=this.sensor.clone(bleData);
        return bleData;
    }

    /**
     * Class which defines the sensor generating the data received
     */
    protected class Sensor{
        //region base
        private String sensor_type;
        private String description;
        //endregion

        //region observationProperty
        //region measurementProperty
        private String accuracy;
        private String drift;
        private String measurementRange;
        private String measurementFrequency;
        private String measurementLatency;
        private String precision;
        private String resolution;
        private String responseTime;
        private String selectivity;
        private String detectionLimit;
        private String sampleRate;
        //endregion
        private String condition;
        private String unit;
        //endregion


        public String getSensor_type() {
            return sensor_type;
        }

        public String getDescription() {
            return description;
        }

        public String getAccuracy() {
            return accuracy;
        }

        public String getDrift() {
            return drift;
        }

        public String getMeasurementRange() {
            return measurementRange;
        }

        public String getMeasurementFrequency() {
            return measurementFrequency;
        }

        public String getMeasurementLatency() {
            return measurementLatency;
        }

        public String getPrecision() {
            return precision;
        }

        public String getResolution() {
            return resolution;
        }

        public String getResponseTime() {
            return responseTime;
        }

        public String getSelectivity() {
            return selectivity;
        }

        public String getDetectionLimit() {
            return detectionLimit;
        }

        public String getSampleRate(){
            return sampleRate;
        }

        public String getCondition() {
            return condition;
        }

        public String getUnit() {
            return unit;
        }

        /**
         * Constructor
         * @param sensor_type the type of the sensor
         * @param description human readable description of the sensor
         * @param accuracy accuracy of the measure
         * @param drift drift of the measure
         * @param measurementRange range of the measurement
         * @param measurementFrequency frequency of the measurement
         * @param measurementLatency latency of the measurement
         * @param precision precision of the measure
         * @param resolution resolution of the measure
         * @param responseTime response time of the sensor
         * @param selectivity of the sensor
         * @param detectionLimit limit of the detection
         * @param condition condition of the measure
         * @param sampleRate sample rate of the sensor
         * @param unit unit of the measure
         */
        Sensor( String sensor_type,
                String description,
                String accuracy,
                String drift,
                String measurementRange,
                String measurementFrequency,
                String measurementLatency,
                String precision,
                String resolution,
                String responseTime,
                String selectivity,
                String detectionLimit,
                String condition,
                String sampleRate,
                String unit){
            this.sensor_type=sensor_type;
            this.description=description;
            this.accuracy=accuracy;
            this.drift=drift;
            this.measurementRange=measurementRange;
            this.measurementFrequency=measurementFrequency;
            this.measurementLatency=measurementLatency;
            this.precision=precision;
            this.resolution=resolution;
            this.responseTime=responseTime;
            this.selectivity=selectivity;
            this.detectionLimit=detectionLimit;
            this.sampleRate=sampleRate;
            this.condition=condition;
            this.unit=unit;
            BLEData.this.sensor=this;
        }

        /**
         * Clones the {@link Sensor}
         */
        public Sensor clone(BLEData bleData){
            return bleData.new Sensor(sensor_type,
                    description,
                    accuracy,
                    drift,
                    measurementRange,
                    measurementFrequency,
                    measurementLatency,
                    precision,
                    resolution,
                    responseTime,
                    selectivity,
                    detectionLimit,
                    condition,
                    sampleRate,
                    unit);
        }
    }

    public static class Builder {

        protected String data_type;
        protected Sensor sensor;

        //region Sensor attributes
        //region general
        protected String sensor_type;
        protected String description;
        //endregion

        //region observationProperty
        //region measurementProperty
        protected String accuracy;
        protected String drift;
        protected String measurementRange;
        protected String measurementFrequency;
        protected String measurementLatency;
        protected String precision;
        protected String resolution;
        protected String responseTime;
        protected String selectivity;
        protected String detectionLimit;
        protected String sampleRate;
        //endregion
        protected String condition;
        protected String unit;
        //endregion
        //endregion


        public BLEData build(){

            int type=ParsingComplements.getDataTypeIntFromString(data_type);
            BLEData bleData=new BLEData(type);
            if (type== ParsingComplements.DT_SENSOR)
                bleData.new Sensor(sensor_type,
                        description,
                        accuracy,
                        drift,
                        measurementRange,
                        measurementFrequency,
                        measurementLatency,
                        precision,
                        resolution,
                        responseTime,
                        selectivity,
                        detectionLimit,
                        condition,
                        sampleRate,
                        unit);
            return bleData;
        }

        public Builder setData_type(String data_type) {
            this.data_type = data_type;
            return this;
        }


        public Builder setSensor_type(String sensor_type) {
            this.sensor_type = sensor_type;
            return this;
        }

        public Builder setSensorDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setSensorAccuracy(String accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        public Builder setSensorDrift(String drift) {
            this.drift = drift;
            return this;
        }

        public Builder setSensorMeasurementRange(String measurementRange) {
            this.measurementRange = measurementRange;
            return this;
        }

        public Builder setSensorMeasurementFrequency(String measurementFrequency) {
            this.measurementFrequency = measurementFrequency;
            return this;
        }

        public Builder setSensorMeasurementLatency(String measurementLatency) {
            this.measurementLatency = measurementLatency;
            return this;
        }

        public Builder setSensorPrecision(String precision) {
            this.precision = precision;
            return this;
        }

        public Builder setSensorResolution(String resolution) {
            this.resolution = resolution;
            return this;
        }

        public Builder setSensorResponseTime(String responseTime) {
            this.responseTime = responseTime;
            return this;
        }

        public Builder setSensorSelectivity(String selectivity) {
            this.selectivity = selectivity;
            return this;
        }

        public Builder setSensorDetectionLimit(String detectionLimit) {
            this.detectionLimit = detectionLimit;
            return this;
        }

        public Builder setSensorSampleRate(String sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }

        public Builder setSensorCondition(String condition) {
            this.condition = condition;
            return this;
        }

        public Builder setSensorUnit(String unit) {
            this.unit = unit;
            return this;
        }
    }

}
