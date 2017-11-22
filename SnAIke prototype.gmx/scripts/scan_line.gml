///scan_line(dir)
var direct = argument0
var xx = obj_head.x
var yy = obj_head.y
switch(direct){
    
    case 0:
    for (j = xx+1; j < 32; j++){
        if position_meeting(j,yy,obj_head) || position_meeting(j,yy,obj_body) {
            return j-xx
        }
    }
    return 32-xx
    break
    
    case 1:
    for (j = yy-1; j > -1; j--){
        if position_meeting(xx,j,obj_head) || position_meeting(xx,j,obj_body) {
            return yy-j
        }
    }
    return yy
    break
    
    case 2:
    for (j = xx-1; j > -1; j--){
        if position_meeting(j,yy,obj_head) || position_meeting(j,yy,obj_body) {
            return xx-j
        }
    }
    return xx
    break
    
    case 3:
    for (j = yy+1; j < 32; j++){
        if position_meeting(xx,j,obj_head) || position_meeting(xx,j,obj_body) {
            return j-yy
        }
    }
    return 32-yy
    break

}
