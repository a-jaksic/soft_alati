package rs.ac.bg.fon.aleksa_jaksic.sa.workday.controller;

import rs.ac.bg.fon.aleksa_jaksic.sa.workday.dtos.WorkDayCreateUpdateDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.dtos.WorkDayDTO;
import rs.ac.bg.fon.aleksa_jaksic.sa.workday.service.WorkDayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WorkDayController {

    private final WorkDayService workDayService;

    public WorkDayController(WorkDayService workDayService){
        this.workDayService = workDayService;
    }

    @GetMapping("/api/restaurants/{id}/workdays")
    public ResponseEntity<?> list(@PathVariable Long id){
        try {
            List<WorkDayDTO> workDayList = workDayService.list(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(workDayList);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the retrieval of workdays was not successful!");
        }
    }

    @PostMapping("/api/admin/restaurants/{id}/workdays/create")
    public ResponseEntity<?> create(@PathVariable Long id,@RequestBody WorkDayCreateUpdateDTO workDayCreateUpdateDTO){
        try {
            WorkDayDTO workDay = workDayService.create(id, workDayCreateUpdateDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(workDay);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the workday creation was not successful!");
        }
    }

    @PatchMapping("/api/admin/restaurants/workdays/{id}")
    public ResponseEntity<?> change(@PathVariable Long id, @RequestBody WorkDayCreateUpdateDTO workDayCreateUpdateDTO){
        try {
            WorkDayDTO workDayDTO = workDayService.update(id, workDayCreateUpdateDTO);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(workDayDTO);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the workday change was not successful!");
        }
    }

    @DeleteMapping("/api/admin/restaurants/workdays/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        try {
            workDayService.delete(id);
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error, the workday deletion was not successful!");
        }
    }

}

