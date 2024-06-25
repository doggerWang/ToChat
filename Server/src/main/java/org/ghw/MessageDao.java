package org.ghw;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;

@Mapper
public interface MessageDao {
    @Insert("insert into ToChat.Message (type,name,time,text) values(#{type},#{name},#{time},#{text})")
    public void insert(Message message);
    @Select("select * from ToChat.Message ")
    public ArrayList<Message> select();
}
