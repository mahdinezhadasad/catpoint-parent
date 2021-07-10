package com.udacity.catpoint.security.service.service;

import com.udacity.catpoint.image.service.service.ImageService;
import com.udacity.catpoint.security.service.application.StatusListener;
import com.udacity.catpoint.security.service.data.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class SecurityServiceTest {

    @Mock
    ImageService imageService;
    @Mock
    StatusListener statusListener;
    @Mock
    SecurityRepository securityRepository;

    private SecurityService securityService;
    private Sensor sensor;

    private final String random= UUID.randomUUID().toString();

@NotNull

private Sensor createSensor(){


    return new Sensor(random, SensorType.DOOR);
}

@NotNull
private Set<Sensor> getAllsensor(int count, boolean status){

    Set<Sensor> sensors = new HashSet<>();

    for(int i = 0; i<count; i++){


        sensors.add(new Sensor(random,SensorType.DOOR));

    }

    sensors.forEach(sensor ->sensor.setActive(status));

    return sensors;


}

    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);
        sensor = createSensor();
    }



@Test
void aralarmArmedAndSensorBecomeActivated_ChangeStatusToPending(){
    when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
    when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

    securityService.changeSensorActivationStatus(sensor,true);
    
    verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
}

@Test
void armedAndSensorActivatedAndpending_changeStatusToAlaram(){


    when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
    when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
    securityService.changeSensorActivationStatus(sensor, true);

    verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);
}

@Test
void pendingAlarmandSensorInactive_ReturnNoAlarmState(){


    when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
    sensor.setActive(false);
    securityService.changeSensorActivationStatus(sensor,false);

    verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);



}

@ParameterizedTest
@ValueSource(booleans = {true,false})
void alarmIsActive_changeSensorShouldNotChangAlarm(boolean status){

    when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
    securityService.changeSensorActivationStatus(sensor,status);
    verify(securityRepository,never()).setAlarmStatus(any(AlarmStatus.class));

}
@Test
void sensorIsActiveSystemPendingState_changeToAlaramState(){

    when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
    securityService.changeSensorActivationStatus(sensor,true);
    verify(securityRepository,times(1)).setAlarmStatus(any(AlarmStatus.class));

}

@Test
void check_whenInactiveSensorDeactived_doNotChangeAlarm(){

    sensor.setActive(false);
    when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
    securityService.changeSensorActivationStatus(sensor,false);
    verify(securityRepository,never()).setAlarmStatus(any(AlarmStatus.class));



}
@Test
void imageContainCat_systemIsArmedTurnToAlarmStatus(){
   securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
   when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
   when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(true);

    securityService.processImage(mock(BufferedImage.class));

    verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);}

    @Test
    void catNotDetected_changeStatusToNoAlarmAndSytemInactive(){

        Set<Sensor> sensors = getAllsensor(3, false);
        when(securityRepository.getSensors()).thenReturn(sensors);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        securityService.processImage(mock(BufferedImage.class));

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);



    }


    @Test
    void ifSystemDisarmed_setStatusToNoAlarm(){
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void ifSystemArmed_resetAllSensorsToInactive(){
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService.getSensors().forEach(sensor1 -> {
            assert Boolean.FALSE.equals(sensor1.getActive());
        });
    }

    @Test
    void systemArmed_whenCatDetected_setAlarmStatusToAlarm(){
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat()))
                .thenReturn(Boolean.TRUE);
        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository, times(1)).setAlarmStatus(any(AlarmStatus.class));
    }



    @Test
    void testAddAndRemoveStatusListener() {
        securityService.addStatusListener(statusListener);
        securityService.removeStatusListener(statusListener);
    }

    @Test
    void testAddAndRemoveSensor() {
        securityService.addSensor(sensor);
        securityService.removeSensor(sensor);
    }

    @Test
    void checkChangeSensorActivationStatusWorks_withHandleSensorDeactivatedCovered(){
        Sensor sensor1 = new Sensor("testSensor",SensorType.DOOR);
        sensor1.setActive(true);
        securityRepository.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        assert sensor.getActive().equals(false);
        verify(securityRepository, times(1)).setAlarmStatus(any(AlarmStatus.class));
    }


    @ParameterizedTest
    @EnumSource(ArmingStatus.class)
    public void setArmingStatusMethod_runsThreeTimes(ArmingStatus armingStatus) {

        securityService.setArmingStatus(armingStatus);

    }


    @Test
    void test_handSensorActivated_whenRepositoryDisarmed_andAlarmOn_shouldTriggerHandleSensorDeactivated(){
        securityRepository.setArmingStatus(ArmingStatus.DISARMED);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, false);
    }

    @Test
    void test_handSensorActivated_whenRepositoryDisarmed_andRepositoryAlarmPending_shouldTriggerHandleSensorDeactivated(){
        securityRepository.setArmingStatus(ArmingStatus.DISARMED);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, false);
    }


    @Test
    void test_handSensorActivated_whenRepositoryDisarmed_andNoAlarm_shouldTriggerHandleSensorActivated(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        sensor.setActive(false);
        securityService.changeSensorActivationStatus(sensor, true);
    }

}