package com.gmail.razandale.shiftmanagement;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * Слушатель менеджера смен который будет уведомляться об изменении состояния
 * смены контролируемой менеджером.
 *
 * @author RazumnovAA
 */
public interface ShiftManagerListener {

    /**
     * Метод вызываемый у слушателя изменений состояния смены.
     *
     * @param shift смена состояние которой было изменено.
     */
    public void onShiftChaned(Shift shift);

}
