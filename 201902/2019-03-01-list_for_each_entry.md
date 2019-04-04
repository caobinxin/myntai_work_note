# list_for_each_entry

```c
#define list_for_each_entry(pos, head, member)              \
    for (pos = list_first_entry(head, typeof(*pos), member);    \                                                                                             
         &pos->member != (head);                    \
         pos = list_next_entry(pos, member))

#define list_first_entry(ptr, type, member) \                                                                                                                 
    list_entry((ptr)->next, type, member)

             
#define list_next_entry(pos, member) \                                                                                                                        
    list_entry((pos)->member.next, typeof(*(pos)), member)

#define list_entry(ptr, type, member) \                                                                                                                       
    container_of(ptr, type, member)

             
             
 list_for_each_entry(o, &ts_ops, list) {                                               
         
}

for (o = container_of((&ts_ops)->next, typeof(*o), list);&o->list != (&ts_ops);o = container_of((o)->list.next,typeof(*(o)), list))




 for (o = container_of((&ts_ops)->next, typeof(*o), list);
      &o->list != (&ts_ops);               
      o = container_of((o)->list.next,typeof(*(o)), list))
 {
     
 }
     
```

分析这段代码

```c
struct ts_ops {
    const char      *name;
    struct ts_config *  (*init)(const void *, unsigned int, gfp_t, int);
    unsigned int        (*find)(struct ts_config *,
                    struct ts_state *);
    void            (*destroy)(struct ts_config *);
    void *          (*get_pattern)(struct ts_config *);
    unsigned int        (*get_pattern_len)(struct ts_config *);
    struct module       *owner;
    struct list_head    list;
};


static struct ts_ops kmp_ops = { 
    .name         = "kmp",
    .find         = kmp_find,
    .init         = kmp_init,
    .get_pattern      = kmp_get_pattern,
    .get_pattern_len  = kmp_get_pattern_len,
    .owner        = THIS_MODULE,
    .list         = LIST_HEAD_INIT(kmp_ops.list)
};

static int __init init_kmp(void)
{
    return textsearch_register(&kmp_ops);                                                                                                                     
}

```



```c


struct list_head {                                                                 
    struct list_head *next, *prev;
};

#define LIST_HEAD_INIT(name) { &(name), &(name) }
#define LIST_HEAD(name) struct list_head name = LIST_HEAD_INIT(name)

static LIST_HEAD(ts_ops);
//等价于：
/*
	struct list_head ts_ops = { &(ts_ops), &(ts_ops) };
*/

int textsearch_register(struct ts_ops *ops)
{
    int err = -EEXIST;
    struct ts_ops *o;

    if (ops->name == NULL || ops->find == NULL || ops->init == NULL ||
        ops->get_pattern == NULL || ops->get_pattern_len == NULL)
        return -EINVAL;

    spin_lock(&ts_mod_lock);
#if 0
    list_for_each_entry(o, &ts_ops, list) {
        if (!strcmp(ops->name, o->name))
            goto errout;
    }
#else
    for (o = container_of((&ts_ops)->next, typeof(*o), list);&o->list != (&ts_ops);
         o = container_of((o)->list.next,typeof(*(o)), list))
    {
        
        if (!strcmp(ops->name, o->name))
            goto errout;
    }
#endif

    list_add_tail_rcu(&ops->list, &ts_ops);                                                                                                                   
    err = 0;
errout:
    spin_unlock(&ts_mod_lock);
    return err;
}
EXPORT_SYMBOL(textsearch_register);


static inline void list_add_tail_rcu(struct list_head *new,                                                                                                   
                    struct list_head *head)
{       
    __list_add_rcu(new, head->prev, head);
}

static inline void __list_add_rcu(struct list_head *new,                                                                                                      
        struct list_head *prev, struct list_head *next)
{
    if (!__list_add_valid(new, prev, next))
        return;

    new->next = next;
    new->prev = prev;
    rcu_assign_pointer(list_next_rcu(prev), new);
    next->prev = new;
}

static inline bool __list_add_valid(struct list_head *new,                                                                                                    
                struct list_head *prev,
                struct list_head *next)
{
    return true;
}

```

