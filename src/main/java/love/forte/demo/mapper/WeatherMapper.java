package love.forte.demo.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface WeatherMapper {
    @Select("""
            select Location_ID from weather where Location_Name_ZH=#{Location_Name_ZH}
            """)
    String getCityLocation(String Location_Name_ZH);
}
