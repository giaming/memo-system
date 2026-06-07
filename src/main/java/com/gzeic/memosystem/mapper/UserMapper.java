package com.gzeic.memosystem.mapper;

import com.gzeic.memosystem.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author jml
* @description 针对表【user】的数据库操作Mapper
* @createDate 2026-02-04 09:40:41
* @Entity com.gzeic.memosystem.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




