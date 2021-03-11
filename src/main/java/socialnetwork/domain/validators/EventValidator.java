package socialnetwork.domain.validators;

import socialnetwork.domain.FeedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventValidator implements Validator<FeedEvent>  {
    @Override
    public void validate(FeedEvent entity) throws ValidationException {
        List<String> errors = new ArrayList<>();
        if(entity.getTitle().equals("")){
            errors.add("Titlul nu poate fi vid");
        }
        if(entity.getDescription().equals("")){
            errors.add("Descrierea nu poate fi vida");
        }
        if(entity.getDescription().length() > 499){
            errors.add("Descrierea trebuie sa aiba maxim 500 ch.");
        }
        if(entity.getStartDate().isBefore(LocalDateTime.now())){
            errors.add("Data este invalida.");
        }
        if(!errors.isEmpty()){
            throw new ValidationException(errors);
        }
    }
}
