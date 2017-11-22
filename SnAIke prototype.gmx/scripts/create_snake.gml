///create_snake()
randomize()
switch(irandom(3)){
    case 0: instance_create(6,29,obj_head) break
    case 1: instance_create(6,3,obj_head) break
    case 2: instance_create(25,29,obj_head) break
    case 3: instance_create(25,3,obj_head) break
}
