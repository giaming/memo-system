package com.gzeic.memosystem.mapper;

import com.gzeic.memosystem.entity.Memo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author jml
* @description 针对表【memo】的数据库操作Mapper
* @createDate 2026-02-04 09:40:13
* @Entity com.gzeic.memosystem.entity.Memo
*/
@Mapper
public interface MemoMapper extends BaseMapper<Memo> {

}




