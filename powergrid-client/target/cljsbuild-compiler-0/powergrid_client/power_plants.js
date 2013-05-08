goog.provide('powergrid_client.power_plants');
goog.require('cljs.core');
goog.provide('powergrid_client.power_plants.PowerPlant');

/**
* @constructor
* @param {*} number
* @param {*} resource
* @param {*} capacity
* @param {*} yield$
* @param {*} __meta
* @param {*} __extmap
* @param {*=} __meta 
* @param {*=} __extmap
*/
powergrid_client.power_plants.PowerPlant = (function (number,resource,capacity,yield$,__meta,__extmap){
this.number = number;
this.resource = resource;
this.capacity = capacity;
this.yield$ = yield$;
this.__meta = __meta;
this.__extmap = __extmap;
this.cljs$lang$protocol_mask$partition1$ = 0;
this.cljs$lang$protocol_mask$partition0$ = 2229667594;
if(arguments.length>4){
this.__meta = __meta;
this.__extmap = __extmap;
} else {
this.__meta=null;
this.__extmap=null;
}
})
powergrid_client.power_plants.PowerPlant.prototype.cljs$core$IHash$_hash$arity$1 = (function (this__2396__auto__){
var self__ = this;
var h__2264__auto__ = self__.__hash;
if(!((h__2264__auto__ == null)))
{return h__2264__auto__;
} else
{var h__2264__auto____$1 = cljs.core.hash_imap.call(null,this__2396__auto__);
self__.__hash = h__2264__auto____$1;
return h__2264__auto____$1;
}
});
powergrid_client.power_plants.PowerPlant.prototype.cljs$core$ILookup$_lookup$arity$2 = (function (this__2401__auto__,k__2402__auto__){
var self__ = this;
return this__2401__auto__.cljs$core$ILookup$_lookup$arity$3(this__2401__auto__,k__2402__auto__,null);
});
powergrid_client.power_plants.PowerPlant.prototype.cljs$core$ILookup$_lookup$arity$3 = (function (this__2403__auto__,k3153,else__2404__auto__){
var self__ = this;
if((k3153 === "\uFDD0'number"))
{return self__.number;
} else
{if((k3153 === "\uFDD0'resource"))
{return self__.resource;
} else
{if((k3153 === "\uFDD0'capacity"))
{return self__.capacity;
} else
{if((k3153 === "\uFDD0'yield"))
{return self__.yield$;
} else
{if("\uFDD0'else")
{return cljs.core._lookup.call(null,self__.__extmap,k3153,else__2404__auto__);
} else
{return null;
}
}
}
}
}
});
powergrid_client.power_plants.PowerPlant.prototype.cljs$core$IAssociative$_assoc$arity$3 = (function (this__2408__auto__,k__2409__auto__,G__3152){
var self__ = this;
var pred__3155 = cljs.core.identical_QMARK_;
var expr__3156 = k__2409__auto__;
if(pred__3155.call(null,"\uFDD0'number",expr__3156))
{return (new powergrid_client.power_plants.PowerPlant(G__3152,self__.resource,self__.capacity,self__.yield$,self__.__meta,self__.__extmap,null));
} else
{if(pred__3155.call(null,"\uFDD0'resource",expr__3156))
{return (new powergrid_client.power_plants.PowerPlant(self__.number,G__3152,self__.capacity,self__.yield$,self__.__meta,self__.__extmap,null));
} else
{if(pred__3155.call(null,"\uFDD0'capacity",expr__3156))
{return (new powergrid_client.power_plants.PowerPlant(self__.number,self__.resource,G__3152,self__.yield$,self__.__meta,self__.__extmap,null));
} else
{if(pred__3155.call(null,"\uFDD0'yield",expr__3156))
{return (new powergrid_client.power_plants.PowerPlant(self__.number,self__.resource,self__.capacity,G__3152,self__.__meta,self__.__extmap,null));
} else
{return (new powergrid_client.power_plants.PowerPlant(self__.number,self__.resource,self__.capacity,self__.yield$,self__.__meta,cljs.core.assoc.call(null,self__.__extmap,k__2409__auto__,G__3152),null));
}
}
}
}
});
powergrid_client.power_plants.PowerPlant.prototype.cljs$core$IPrintWithWriter$_pr_writer$arity$3 = (function (this__2415__auto__,writer__2416__auto__,opts__2417__auto__){
var self__ = this;
var pr_pair__2418__auto__ = (function (keyval__2419__auto__){
return cljs.core.pr_sequential_writer.call(null,writer__2416__auto__,cljs.core.pr_writer,""," ","",opts__2417__auto__,keyval__2419__auto__);
});
return cljs.core.pr_sequential_writer.call(null,writer__2416__auto__,pr_pair__2418__auto__,[cljs.core.str("#"),cljs.core.str("PowerPlant"),cljs.core.str("{")].join(''),", ","}",opts__2417__auto__,cljs.core.concat.call(null,cljs.core.PersistentVector.fromArray([cljs.core.vector.call(null,"\uFDD0'number",self__.number),cljs.core.vector.call(null,"\uFDD0'resource",self__.resource),cljs.core.vector.call(null,"\uFDD0'capacity",self__.capacity),cljs.core.vector.call(null,"\uFDD0'yield",self__.yield$)], true),self__.__extmap));
});
powergrid_client.power_plants.PowerPlant.prototype.cljs$core$ICollection$_conj$arity$2 = (function (this__2406__auto__,entry__2407__auto__){
var self__ = this;
if(cljs.core.vector_QMARK_.call(null,entry__2407__auto__))
{return this__2406__auto__.cljs$core$IAssociative$_assoc$arity$3(this__2406__auto__,cljs.core._nth.call(null,entry__2407__auto__,0),cljs.core._nth.call(null,entry__2407__auto__,1));
} else
{return cljs.core.reduce.call(null,cljs.core._conj,this__2406__auto__,entry__2407__auto__);
}
});
powergrid_client.power_plants.PowerPlant.prototype.cljs$core$ISeqable$_seq$arity$1 = (function (this__2413__auto__){
var self__ = this;
return cljs.core.seq.call(null,cljs.core.concat.call(null,cljs.core.PersistentVector.fromArray([cljs.core.vector.call(null,"\uFDD0'number",self__.number),cljs.core.vector.call(null,"\uFDD0'resource",self__.resource),cljs.core.vector.call(null,"\uFDD0'capacity",self__.capacity),cljs.core.vector.call(null,"\uFDD0'yield",self__.yield$)], true),self__.__extmap));
});
powergrid_client.power_plants.PowerPlant.prototype.cljs$core$ICounted$_count$arity$1 = (function (this__2405__auto__){
var self__ = this;
return (4 + cljs.core.count.call(null,self__.__extmap));
});
powergrid_client.power_plants.PowerPlant.prototype.cljs$core$IEquiv$_equiv$arity$2 = (function (this__2397__auto__,other__2398__auto__){
var self__ = this;
if(cljs.core.truth_((function (){var and__3822__auto__ = other__2398__auto__;
if(cljs.core.truth_(and__3822__auto__))
{var and__3822__auto____$1 = (this__2397__auto__.constructor === other__2398__auto__.constructor);
if(and__3822__auto____$1)
{return cljs.core.equiv_map.call(null,this__2397__auto__,other__2398__auto__);
} else
{return and__3822__auto____$1;
}
} else
{return and__3822__auto__;
}
})()))
{return true;
} else
{return false;
}
});
powergrid_client.power_plants.PowerPlant.prototype.cljs$core$IWithMeta$_with_meta$arity$2 = (function (this__2400__auto__,G__3152){
var self__ = this;
return (new powergrid_client.power_plants.PowerPlant(self__.number,self__.resource,self__.capacity,self__.yield$,G__3152,self__.__extmap,self__.__hash));
});
powergrid_client.power_plants.PowerPlant.prototype.cljs$core$IMeta$_meta$arity$1 = (function (this__2399__auto__){
var self__ = this;
return self__.__meta;
});
powergrid_client.power_plants.PowerPlant.prototype.cljs$core$IMap$_dissoc$arity$2 = (function (this__2410__auto__,k__2411__auto__){
var self__ = this;
if(cljs.core.contains_QMARK_.call(null,cljs.core.PersistentHashSet.fromArray(["\uFDD0'yield","\uFDD0'resource","\uFDD0'capacity","\uFDD0'number"]),k__2411__auto__))
{return cljs.core.dissoc.call(null,cljs.core.with_meta.call(null,cljs.core.into.call(null,cljs.core.ObjMap.EMPTY,this__2410__auto__),self__.__meta),k__2411__auto__);
} else
{return (new powergrid_client.power_plants.PowerPlant(self__.number,self__.resource,self__.capacity,self__.yield$,self__.__meta,cljs.core.not_empty.call(null,cljs.core.dissoc.call(null,self__.__extmap,k__2411__auto__)),null));
}
});
powergrid_client.power_plants.PowerPlant.cljs$lang$type = true;
powergrid_client.power_plants.PowerPlant.cljs$lang$ctorPrSeq = (function (this__2436__auto__){
return cljs.core.list.call(null,"powergrid-client.power-plants/PowerPlant");
});
powergrid_client.power_plants.PowerPlant.cljs$lang$ctorPrWriter = (function (this__2436__auto__,writer__2437__auto__){
return cljs.core._write.call(null,writer__2437__auto__,"powergrid-client.power-plants/PowerPlant");
});
powergrid_client.power_plants.__GT_PowerPlant = (function __GT_PowerPlant(number,resource,capacity,yield$){
return (new powergrid_client.power_plants.PowerPlant(number,resource,capacity,yield$));
});
powergrid_client.power_plants.map__GT_PowerPlant = (function map__GT_PowerPlant(G__3154){
return (new powergrid_client.power_plants.PowerPlant((new cljs.core.Keyword("\uFDD0'number")).call(null,G__3154),(new cljs.core.Keyword("\uFDD0'resource")).call(null,G__3154),(new cljs.core.Keyword("\uFDD0'capacity")).call(null,G__3154),(new cljs.core.Keyword("\uFDD0'yield")).call(null,G__3154),null,cljs.core.dissoc.call(null,G__3154,"\uFDD0'number","\uFDD0'resource","\uFDD0'capacity","\uFDD0'yield")));
});
powergrid_client.power_plants.id = (function id(power_plant){
return (new cljs.core.Keyword("\uFDD0'number")).call(null,power_plant);
});
powergrid_client.power_plants.capacity = (function capacity(power_plant){
return (new cljs.core.Keyword("\uFDD0'capacity")).call(null,power_plant);
});
powergrid_client.power_plants.max_capacity = (function max_capacity(power_plant){
return (2 * powergrid_client.power_plants.capacity.call(null,power_plant));
});
powergrid_client.power_plants.yield$ = (function yield$(power_plant){
return (new cljs.core.Keyword("\uFDD0'yield")).call(null,power_plant);
});
powergrid_client.power_plants.plant_number = (function plant_number(power_plant){
return (new cljs.core.Keyword("\uFDD0'number")).call(null,power_plant);
});
powergrid_client.power_plants.power_plants = cljs.core.PersistentHashMap.fromArrays([32,33,34,3,35,4,36,5,37,6,38,7,39,8,40,9,10,42,11,12,44,13,14,46,15,16,17,18,50,19,20,21,22,23,24,25,26,27,28,29,30,31],[(new powergrid_client.power_plants.PowerPlant(32,"\uFDD0'oil",3,6)),(new powergrid_client.power_plants.PowerPlant(33,"\uFDD0'ecological",0,4)),(new powergrid_client.power_plants.PowerPlant(34,"\uFDD0'uranium",1,5)),(new powergrid_client.power_plants.PowerPlant(3,"\uFDD0'oil",2,1)),(new powergrid_client.power_plants.PowerPlant(35,"\uFDD0'oil",1,5)),(new powergrid_client.power_plants.PowerPlant(4,"\uFDD0'coal",2,1)),(new powergrid_client.power_plants.PowerPlant(36,"\uFDD0'coal",3,7)),(new powergrid_client.power_plants.PowerPlant(5,cljs.core.PersistentHashSet.fromArray(["\uFDD0'coal","\uFDD0'oil"]),2,1)),(new powergrid_client.power_plants.PowerPlant(37,"\uFDD0'ecological",0,4)),(new powergrid_client.power_plants.PowerPlant(6,"\uFDD0'garbage",1,1)),(new powergrid_client.power_plants.PowerPlant(38,"\uFDD0'garbage",3,7)),(new powergrid_client.power_plants.PowerPlant(7,"\uFDD0'oil",3,2)),(new powergrid_client.power_plants.PowerPlant(39,"\uFDD0'uranium",1,6)),(new powergrid_client.power_plants.PowerPlant(8,"\uFDD0'coal",3,2)),(new powergrid_client.power_plants.PowerPlant(40,"\uFDD0'oil",2,6)),(new powergrid_client.power_plants.PowerPlant(9,"\uFDD0'oil",1,1)),(new powergrid_client.power_plants.PowerPlant(10,"\uFDD0'coal",2,2)),(new powergrid_client.power_plants.PowerPlant(42,"\uFDD0'coal",2,6)),(new powergrid_client.power_plants.PowerPlant(11,"\uFDD0'uranium",1,2)),(new powergrid_client.power_plants.PowerPlant(12,cljs.core.PersistentHashSet.fromArray(["\uFDD0'coal","\uFDD0'oil"]),2,2)),(new powergrid_client.power_plants.PowerPlant(44,"\uFDD0'ecological",0,5)),(new powergrid_client.power_plants.PowerPlant(13,"\uFDD0'ecological",0,1)),(new powergrid_client.power_plants.PowerPlant(14,"\uFDD0'garbage",2,2)),(new powergrid_client.power_plants.PowerPlant(46,cljs.core.PersistentHashSet.fromArray(["\uFDD0'coal","\uFDD0'oil"]),3,7)),(new powergrid_client.power_plants.PowerPlant(15,"\uFDD0'coal",2,3)),(new powergrid_client.power_plants.PowerPlant(16,"\uFDD0'oil",2,3)),(new powergrid_client.power_plants.PowerPlant(17,"\uFDD0'uranium",1,2)),(new powergrid_client.power_plants.PowerPlant(18,"\uFDD0'ecological",0,2)),(new powergrid_client.power_plants.PowerPlant(50,"\uFDD0'fusion",0,6)),(new powergrid_client.power_plants.PowerPlant(19,"\uFDD0'garbage",2,3)),(new powergrid_client.power_plants.PowerPlant(20,"\uFDD0'coal",3,5)),(new powergrid_client.power_plants.PowerPlant(21,cljs.core.PersistentHashSet.fromArray(["\uFDD0'coal","\uFDD0'oil"]),2,4)),(new powergrid_client.power_plants.PowerPlant(22,"\uFDD0'ecological",0,2)),(new powergrid_client.power_plants.PowerPlant(23,"\uFDD0'uranium",1,3)),(new powergrid_client.power_plants.PowerPlant(24,"\uFDD0'garbage",2,4)),(new powergrid_client.power_plants.PowerPlant(25,"\uFDD0'coal",2,5)),(new powergrid_client.power_plants.PowerPlant(26,"\uFDD0'oil",2,5)),(new powergrid_client.power_plants.PowerPlant(27,"\uFDD0'ecological",0,3)),(new powergrid_client.power_plants.PowerPlant(28,"\uFDD0'uranium",1,4)),(new powergrid_client.power_plants.PowerPlant(29,cljs.core.PersistentHashSet.fromArray(["\uFDD0'coal","\uFDD0'oil"]),1,4)),(new powergrid_client.power_plants.PowerPlant(30,"\uFDD0'garbage",3,6)),(new powergrid_client.power_plants.PowerPlant(31,"\uFDD0'coal",3,6))]);
powergrid_client.power_plants.initial_market = (function initial_market(){
return cljs.core.map.call(null,powergrid_client.power_plants.power_plants,cljs.core.PersistentVector.fromArray([3,4,5,6], true));
});
powergrid_client.power_plants.initial_future = (function initial_future(){
return cljs.core.map.call(null,powergrid_client.power_plants.power_plants,cljs.core.PersistentVector.fromArray([7,8,9,10], true));
});
powergrid_client.power_plants.initial_deck = (function initial_deck(){
return cljs.core.keep.call(null,(function (p1__3158_SHARP_){
if((cljs.core.key.call(null,p1__3158_SHARP_) > 10))
{return cljs.core.val.call(null,p1__3158_SHARP_);
} else
{return null;
}
}),powergrid_client.power_plants.power_plants);
});
powergrid_client.power_plants.plant = (function plant(plant_num){
return powergrid_client.power_plants.power_plants.call(null,plant_num);
});
powergrid_client.power_plants.min_price = (function min_price(plant){
return (new cljs.core.Keyword("\uFDD0'number")).call(null,plant);
});
/**
* Returns true if power-plant is hybrid, otherwise false
*/
powergrid_client.power_plants.is_hybrid_QMARK_ = (function is_hybrid_QMARK_(power_plant){
return cljs.core.set_QMARK_.call(null,(new cljs.core.Keyword("\uFDD0'resource")).call(null,power_plant));
});
/**
* Returns true if plant requires resources to operate, otherwise false.
*/
powergrid_client.power_plants.consumes_resources_QMARK_ = (function consumes_resources_QMARK_(p__3159){
var map__3161 = p__3159;
var map__3161__$1 = ((cljs.core.seq_QMARK_.call(null,map__3161))?cljs.core.apply.call(null,cljs.core.hash_map,map__3161):map__3161);
var resource = cljs.core._lookup.call(null,map__3161__$1,"\uFDD0'resource",null);
return !((function (){var or__3824__auto__ = cljs.core._EQ_.call(null,"\uFDD0'ecological",resource);
if(or__3824__auto__)
{return or__3824__auto__;
} else
{return cljs.core._EQ_.call(null,"\uFDD0'fusion",resource);
}
})());
});
/**
* Returns true if the power-plant accepts the resource, otherwise false
*/
powergrid_client.power_plants.accepts_resource_QMARK_ = (function accepts_resource_QMARK_(p__3162,resource){
var map__3167 = p__3162;
var map__3167__$1 = ((cljs.core.seq_QMARK_.call(null,map__3167))?cljs.core.apply.call(null,cljs.core.hash_map,map__3167):map__3167);
var power_plant = map__3167__$1;
var power_plant_resource = cljs.core._lookup.call(null,map__3167__$1,"\uFDD0'resource",null);
if(cljs.core.truth_(powergrid_client.power_plants.is_hybrid_QMARK_.call(null,power_plant)))
{return cljs.core.contains_QMARK_.call(null,power_plant_resource,resource);
} else
{var pred__3168 = cljs.core._EQ_;
var expr__3169 = power_plant_resource;
if(pred__3168.call(null,"\uFDD0'ecological",expr__3169))
{return false;
} else
{if(pred__3168.call(null,"\uFDD0'fusion",expr__3169))
{return false;
} else
{if(pred__3168.call(null,resource,expr__3169))
{return true;
} else
{return false;
}
}
}
}
});
