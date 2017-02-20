package com.calendar.client.ui;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorError;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Validators {

    static List<EditorError> filterName(Editor<String> editor, String s) {
        List<EditorError> result = new ArrayList<>();
        String value = s == null ? "" : s;
        if (value.length() == 0) {
            result.add(new BasicEditorError(editor, s, "Название фильтра не может быть пустым!"));
        } else if (value.length() > 16) {
            result.add(new BasicEditorError(editor, s, "Название фильтра не может длиннее 16 символов!"));
        }
        return result;
    }

    static class PastValidator implements Validator<Date> {
        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public List<EditorError> validate(Editor<Date> editor,  Date value) {
            List<EditorError> result = new ArrayList<>();
            Date now = new Date();
            if (value == null){
                value = now;
                result.add(new BasicEditorError(editor, value, "Укажите время события!"));
            } else {
                if (value.before(now)) {
                    result.add(new BasicEditorError(editor, value, "Событие не может происходить в прошлом!"));
                }
            }
            return result;
        }
    }

    static class isEmptyValidator <T> implements Validator<T>{
        private String fieldName;

        isEmptyValidator(String name){
            fieldName = name;
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public List<EditorError> validate(Editor<T> editor, T value) {
            List<EditorError> result = new ArrayList<>();
            if (value == null || (value instanceof String && value.equals(""))) {
                result.add(new BasicEditorError(editor, value, fieldName + " не может быть пустым!"));
            }
            return result;
        }
    }

    static class emailValidator implements Validator<String> {
        @Override
        public int getPriority() {
            return 1;
        }

        private boolean checkEmailFormat(String value) {
            return value.matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        }

        @Override
        public List<EditorError> validate(Editor<String> editor, String value) {
            List<EditorError> result = new ArrayList<>();
            if (value == null || !checkEmailFormat(value)) {
                result.add(new BasicEditorError(editor, value, "Неверный формат email."));
            }
            return result;
        }
    }
}
