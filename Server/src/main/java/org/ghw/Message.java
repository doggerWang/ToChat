package org.ghw;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Message {
    private Integer id;
    private String type;
    private String name;
    private String text;
    private String time;
}
