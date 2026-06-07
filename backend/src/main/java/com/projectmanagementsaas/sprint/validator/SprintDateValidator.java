package com.projectmanagementsaas.sprint.validator;

import com.projectmanagementsaas.common.exception.BadRequestException;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class SprintDateValidator {
    public void validate(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("Sprint end date must be on or after start date");
        }
    }
}
