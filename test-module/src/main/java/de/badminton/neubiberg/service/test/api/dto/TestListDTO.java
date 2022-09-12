package de.badminton.neubiberg.service.test.api.dto;

import java.util.AbstractList;

/**
 * TODO Document this class
 */
public class TestListDTO extends AbstractList<TestDTO> {

    private final TestDTO element;

    public TestListDTO(TestDTO element) {
        this.element = element;
    }

    @Override
    public TestDTO get(int index) {
        if(index == 0) {
            return element;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        return 1;
    }
}
