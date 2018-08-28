package com.gmail.razandale.shiftmanagement;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import com.gmail.razandale.jpa.JPAExecutor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

public class ShiftManagerImpl implements ShiftManager {

    private Shift shift;
    private JPAExecutor jpaExecutor;
    private final List<ShiftManagerListener> shiftManagerListeners;

    public ShiftManagerImpl(JPAExecutor jpaExecutor) {
        shiftManagerListeners = new ArrayList<>();
        this.jpaExecutor = jpaExecutor;
        //Получаем количество незавершенных смен.
        List<Shift> shifts = getAllUnfinishedShifts();
        switch (shifts.size()) {
            case 0: {
                shift = new Shift();
                saveCurrentShift();
                break;
            }
            case 1: {
                shift = shifts.get(0);
                break;
            }
            default: {
                JOptionPane.showMessageDialog(
                        null,
                        "В базе найдено более одной незакрытой смены. "
                        + "Первая незакрытая смена будет установлена как текущая.",
                        "Предупреждение",
                        JOptionPane.WARNING_MESSAGE
                );
                shift = shifts.get(0);
            }
        }
    }

    @Override
    public void addListeners(List<ShiftManagerListener> listeners) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Уведомляет всех слушателей об изменении состоянии контролируемой
     * менеджером смены.
     */
    private void notifyAllListenersAboutShiftState() {
        shiftManagerListeners.forEach((listener) -> {
            listener.onShiftChaned(shift);
        });
    }

    private List<Shift> getAllUnfinishedShifts() {
        return jpaExecutor.executeQuery((em) -> {
            return em.createNamedQuery("getAllUnfinishedShifts", Shift.class).getResultList();
        });
    }

    private Shift getShiftById(Long id) {
        return jpaExecutor.executeQuery((em) -> {
            return em.find(Shift.class, id);
        });
    }

    /**
     * Добавляет слушателя изменений состояния смены.
     *
     * @param listener слушатель для добавления в список слушателей.
     */
    @Override
    public void addListener(ShiftManagerListener listener) {
        shiftManagerListeners.add(listener);
        if (shift != null) {
            notifyAllListenersAboutShiftState();
        }
    }

    @Override
    public ShiftState getState() {
        if (shift.getBeginning() == null && shift.getFinish() == null) {
            return ShiftManager.ShiftState.SHIFT_IS_NOT_STARTED;
        }

        if (shift.getBeginning() != null && shift.getFinish() == null) {
            return ShiftManager.ShiftState.SHIFT_IS_RUNNING;
        }

        if (shift.getBeginning() != null && shift.getFinish() != null) {
            return ShiftManager.ShiftState.SHIFT_IS_FINISHED;
        }

        return ShiftManager.ShiftState.SHIFT_IS_IN_A_WRONG_STATE;

    }

    @Override
    public void removeListener(ShiftManagerListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeListeners(List<ShiftManagerListener> listeners) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Shift saveCurrentShift() {
        if (shift != null) {
            return jpaExecutor.merge(shift);
        } else {
            JOptionPane.showMessageDialog(
                    null,
                    "Невозможно сохранить смену, так как в менеджере нет ни одной смены.",
                    "Ошибка",
                    ERROR_MESSAGE
            );
            return null;
        }
    }

    @Override
    public void endShift() throws Exception {
        //Если текущая смена идет.
        switch (getState()) {
            case SHIFT_IS_RUNNING:
                shift.setFinish(new Date());
                saveCurrentShift();
                JOptionPane.showMessageDialog(
                        null,
                        "Смена завершилась.",
                        "Уведомление",
                        JOptionPane.INFORMATION_MESSAGE
                );
                notifyAllListenersAboutShiftState();
                break;
            default:
                throw new Exception("Не возможно завершить смену.");
        }
    }

    @Override
    public Shift getShift() {
        return shift;
    }

    @Override
    public void startShift(Operator operator) throws Exception {
        switch (getState()) {
            case SHIFT_IS_NOT_STARTED:
                throw new Exception("Смена не начата.");
            case SHIFT_IS_FINISHED:
                //Создаем новую смену.
                shift = new Shift();
                //Устанавлиаем оператора смены
                shift.setOperator(operator);
                //Начинаем смену.
                shift.setBeginning(new Date());
                shift = saveCurrentShift();
                JOptionPane.showMessageDialog(
                        null,
                        "Смена началась.",
                        "Уведомление",
                        JOptionPane.INFORMATION_MESSAGE
                );
                notifyAllListenersAboutShiftState();
                break;
            default:
                throw new Exception("Не удалось начать смену.");
        }
    }

}
