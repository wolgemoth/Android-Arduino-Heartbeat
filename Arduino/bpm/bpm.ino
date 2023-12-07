#define USE_ARDUINO_INTERRUPTS true // Set-up low-level interrupts for most accurate BPM math.

#include <PulseSensorPlayground.h>  // Includes the PulseSensorPlayground Library.
#include <SoftwareSerial.h>
#include <List.hpp>

/* VARIABLES */

PulseSensorPlayground m_PulseSensor;  // Creates an instance of the PulseSensorPlayground object called "pulseSensor".

//Courtesy of: https://electropeak.com/learn/tutorial-getting-started-with-hc05-bluetooth-module-arduino/
SoftwareSerial m_Bluetooth(10, 11); // RX | TX 

SingleLinkedList<byte> m_Buffer;

/* HARDWARE PARAMETERS */
const int m_PulseWire   = 0;    // PulseSensor PURPLE WIRE connected to ANALOG PIN.
const int m_BaudRate    = 9600; // Baud Rate.
const int m_PollingRate = 50;   // Polling rate in Hz. Default = 50Hz (20ms)

/* PEAK FILTERING */
int m_PeakThreshold = 950;      // Determine which Signal to "count as a beat" and which to ignore.

bool 
  m_IsPeak = false,
  m_PeakLastTick = false;

int m_Delta = 0,            // Time since last detected heartbeat.
    m_MaxDelta = 60000.0f;  // Clamps delta to this value.

/* ERROR FILTERING */
float m_LastBPM = -1.0f; // Last BPM Value

int    m_MissedBeats = 0, // Number of discarded beats since the last successful beat.
    m_MaxMissedBeats = 5; // Maximum number of discarded beats.

/* DECLARATIONS */

void OnHeartbeat();

/* FUNCTIONS */

void setup() {

  Serial.begin(m_BaudRate); // For Serial Monitor
  m_Bluetooth.begin(m_BaudRate); 

  // Configure the PulseSensor object, by assigning our variables to it.
  m_PulseSensor.analogInput(m_PulseWire);
  
  // Double-check the "pulseSensor" object was created and "began" seeing a signal.
  if (m_PulseSensor.begin() == false) {
    // TODO: Some kind of failsafe.
  }
}

void loop() {

  //Serial.println("1: " + String(analogRead(1)));

  // Flush serial and clear buffer.
  Serial.flush();
  m_Buffer.clear();

  /* COMPUTE DELAY */
  int delayMs = (int)(1000.0f / (float)m_PollingRate);

  // Clamp delta to max value.
  m_Delta += delayMs;
  if (m_Delta > m_MaxDelta) {
      m_Delta = m_MaxDelta;
  }
  
  delay(delayMs);

  /* GET BLUETOOTH INPUT */
  while(m_Bluetooth.available() > 0) {

    byte datum = m_Bluetooth.read();
    
    m_Buffer.add(datum);
  }

  /* PRINT BLUETOOTH INPUT */
  if (m_Buffer.getSize() > 0) {
    String str = m_Buffer.toArray();
    Serial.println(str);
  }

  /* MANUALLY FILTER HEARTRATE */

  // Retrieve raw data from the pulse monitor.
  int rawSignal = analogRead(m_PulseWire);
  
  // Naive peak (local maxima) filtering approach.
  m_IsPeak = rawSignal >= m_PeakThreshold;

  // If a heartbeat is detected:
  if (m_IsPeak && !m_PeakLastTick) {
    OnHeartbeat();
    m_Delta = 0;
  }

  m_PeakLastTick = m_IsPeak;

}

void OnHeartbeat() {
  
  Serial.flush();

  float bpm = 60000.0f / (float)m_Delta;

  const float errorTolerance = 0.3f; // 30%.

  if (m_LastBPM > 0) {
    
      if (m_MissedBeats > m_MaxMissedBeats || 
          abs(bpm - m_LastBPM) < (m_LastBPM * errorTolerance)
        ) {

        String str = String(bpm);
        
             Serial.println("0: " + str);
        m_Bluetooth.println("0: " + str);

        m_LastBPM = bpm;

        m_MissedBeats = 0;
      }
      else {
        m_MissedBeats++;    
      }
  }
  else {
    m_LastBPM = bpm;
  }
}
