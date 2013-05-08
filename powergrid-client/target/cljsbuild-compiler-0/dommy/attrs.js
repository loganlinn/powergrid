goog.provide('dommy.attrs');
goog.require('cljs.core');
goog.require('clojure.string');
/**
* does class-name string have class starting at index idx.
* only will be used when Element::classList doesn't exist
*/
dommy.attrs.class_match_QMARK_ = (function class_match_QMARK_(class_name,class$,idx){
var and__3822__auto__ = (function (){var or__3824__auto__ = (idx === 0);
if(or__3824__auto__)
{return or__3824__auto__;
} else
{return (" " === class_name.charAt((idx - 1)));
}
})();
if(cljs.core.truth_(and__3822__auto__))
{var total_len = class_name.length;
var stop = (idx + class$.length);
if((stop <= total_len))
{var or__3824__auto__ = (stop === total_len);
if(or__3824__auto__)
{return or__3824__auto__;
} else
{return (" " === class_name.charAt(stop));
}
} else
{return null;
}
} else
{return and__3822__auto__;
}
});
/**
* Finds the index of class in a space-delimited class-name
* only will be used when Element::classList doesn't exist
*/
dommy.attrs.class_index = (function class_index(class_name,class$){
var start_from = 0;
while(true){
var i = class_name.indexOf(class$,start_from);
if((i >= 0))
{if(dommy.attrs.class_match_QMARK_.call(null,class_name,class$,i))
{return i;
} else
{{
var G__5119 = (i + class$.length);
start_from = G__5119;
continue;
}
}
} else
{return null;
}
break;
}
});
/**
* Does an HTML element have a class. Uses Element::classList if
* available and otherwise does fast parse of className string
*/
dommy.attrs.has_class_QMARK_ = (function has_class_QMARK_(elem,class$){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var class$__$1 = cljs.core.name.call(null,class$);
var temp__3971__auto__ = elem__$1.classList;
if(cljs.core.truth_(temp__3971__auto__))
{var class_list = temp__3971__auto__;
return class_list.contains(class$__$1);
} else
{var temp__3974__auto__ = elem__$1.className;
if(cljs.core.truth_(temp__3974__auto__))
{var class_name = temp__3974__auto__;
var temp__3974__auto____$1 = dommy.attrs.class_index.call(null,class_name,class$__$1);
if(cljs.core.truth_(temp__3974__auto____$1))
{var i = temp__3974__auto____$1;
return (i >= 0);
} else
{return null;
}
} else
{return null;
}
}
});
/**
* add class to element
* @param {...*} var_args
*/
dommy.attrs.add_class_BANG_ = (function() {
var add_class_BANG_ = null;
var add_class_BANG___2 = (function (elem,classes){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var classes__$1 = clojure.string.trim.call(null,cljs.core.name.call(null,classes));
if(cljs.core.seq.call(null,classes__$1))
{var temp__3971__auto___5126 = elem__$1.classList;
if(cljs.core.truth_(temp__3971__auto___5126))
{var class_list_5127 = temp__3971__auto___5126;
var G__5123_5128 = cljs.core.seq.call(null,classes__$1.split(/\s+/));
while(true){
if(G__5123_5128)
{var class_5129 = cljs.core.first.call(null,G__5123_5128);
class_list_5127.add(class_5129);
{
var G__5130 = cljs.core.next.call(null,G__5123_5128);
G__5123_5128 = G__5130;
continue;
}
} else
{}
break;
}
} else
{var class_name_5131 = elem__$1.className;
var G__5124_5132 = cljs.core.seq.call(null,classes__$1.split(/\s+/));
while(true){
if(G__5124_5132)
{var class_5133 = cljs.core.first.call(null,G__5124_5132);
if(cljs.core.truth_(dommy.attrs.class_index.call(null,class_name_5131,class_5133)))
{} else
{elem__$1.className = (((class_name_5131 === ""))?class_5133:[cljs.core.str(class_name_5131),cljs.core.str(" "),cljs.core.str(class_5133)].join(''));
}
{
var G__5134 = cljs.core.next.call(null,G__5124_5132);
G__5124_5132 = G__5134;
continue;
}
} else
{}
break;
}
}
} else
{}
return elem__$1;
});
var add_class_BANG___3 = (function() { 
var G__5135__delegate = function (elem,classes,more_classes){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var G__5125_5136 = cljs.core.seq.call(null,cljs.core.conj.call(null,more_classes,classes));
while(true){
if(G__5125_5136)
{var c_5137 = cljs.core.first.call(null,G__5125_5136);
add_class_BANG_.call(null,elem__$1,c_5137);
{
var G__5138 = cljs.core.next.call(null,G__5125_5136);
G__5125_5136 = G__5138;
continue;
}
} else
{}
break;
}
return elem__$1;
};
var G__5135 = function (elem,classes,var_args){
var more_classes = null;
if (goog.isDef(var_args)) {
  more_classes = cljs.core.array_seq(Array.prototype.slice.call(arguments, 2),0);
} 
return G__5135__delegate.call(this, elem, classes, more_classes);
};
G__5135.cljs$lang$maxFixedArity = 2;
G__5135.cljs$lang$applyTo = (function (arglist__5139){
var elem = cljs.core.first(arglist__5139);
var classes = cljs.core.first(cljs.core.next(arglist__5139));
var more_classes = cljs.core.rest(cljs.core.next(arglist__5139));
return G__5135__delegate(elem, classes, more_classes);
});
G__5135.cljs$lang$arity$variadic = G__5135__delegate;
return G__5135;
})()
;
add_class_BANG_ = function(elem,classes,var_args){
var more_classes = var_args;
switch(arguments.length){
case 2:
return add_class_BANG___2.call(this,elem,classes);
default:
return add_class_BANG___3.cljs$lang$arity$variadic(elem,classes, cljs.core.array_seq(arguments, 2));
}
throw(new Error('Invalid arity: ' + arguments.length));
};
add_class_BANG_.cljs$lang$maxFixedArity = 2;
add_class_BANG_.cljs$lang$applyTo = add_class_BANG___3.cljs$lang$applyTo;
add_class_BANG_.cljs$lang$arity$2 = add_class_BANG___2;
add_class_BANG_.cljs$lang$arity$variadic = add_class_BANG___3.cljs$lang$arity$variadic;
return add_class_BANG_;
})()
;
dommy.attrs.remove_class_str = (function remove_class_str(init_class_name,class$){
var class_name = init_class_name;
while(true){
var class_len = class_name.length;
var temp__3971__auto__ = dommy.attrs.class_index.call(null,class_name,class$);
if(cljs.core.truth_(temp__3971__auto__))
{var i = temp__3971__auto__;
{
var G__5140 = (function (){var end = (i + class$.length);
return [cljs.core.str((((end < class_len))?[cljs.core.str(class_name.substring(0,i)),cljs.core.str(class_name.substr((end + 1)))].join(''):class_name.substring(0,(i - 1))))].join('');
})();
class_name = G__5140;
continue;
}
} else
{return class_name;
}
break;
}
});
/**
* remove class from and returns `elem`
* @param {...*} var_args
*/
dommy.attrs.remove_class_BANG_ = (function() {
var remove_class_BANG_ = null;
var remove_class_BANG___2 = (function (elem,class$){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var class$__$1 = cljs.core.name.call(null,class$);
var temp__3971__auto___5143 = elem__$1.classList;
if(cljs.core.truth_(temp__3971__auto___5143))
{var class_list_5144 = temp__3971__auto___5143;
class_list_5144.remove(class$__$1);
} else
{var class_name_5145 = elem__$1.className;
var new_class_name_5146 = dommy.attrs.remove_class_str.call(null,class_name_5145,class$__$1);
if((class_name_5145 === new_class_name_5146))
{} else
{elem__$1.className = new_class_name_5146;
}
}
return elem__$1;
});
var remove_class_BANG___3 = (function() { 
var G__5147__delegate = function (elem,class$,classes){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var G__5142 = cljs.core.seq.call(null,cljs.core.conj.call(null,classes,class$));
while(true){
if(G__5142)
{var c = cljs.core.first.call(null,G__5142);
remove_class_BANG_.call(null,elem__$1,c);
{
var G__5148 = cljs.core.next.call(null,G__5142);
G__5142 = G__5148;
continue;
}
} else
{return null;
}
break;
}
};
var G__5147 = function (elem,class$,var_args){
var classes = null;
if (goog.isDef(var_args)) {
  classes = cljs.core.array_seq(Array.prototype.slice.call(arguments, 2),0);
} 
return G__5147__delegate.call(this, elem, class$, classes);
};
G__5147.cljs$lang$maxFixedArity = 2;
G__5147.cljs$lang$applyTo = (function (arglist__5149){
var elem = cljs.core.first(arglist__5149);
var class$ = cljs.core.first(cljs.core.next(arglist__5149));
var classes = cljs.core.rest(cljs.core.next(arglist__5149));
return G__5147__delegate(elem, class$, classes);
});
G__5147.cljs$lang$arity$variadic = G__5147__delegate;
return G__5147;
})()
;
remove_class_BANG_ = function(elem,class$,var_args){
var classes = var_args;
switch(arguments.length){
case 2:
return remove_class_BANG___2.call(this,elem,class$);
default:
return remove_class_BANG___3.cljs$lang$arity$variadic(elem,class$, cljs.core.array_seq(arguments, 2));
}
throw(new Error('Invalid arity: ' + arguments.length));
};
remove_class_BANG_.cljs$lang$maxFixedArity = 2;
remove_class_BANG_.cljs$lang$applyTo = remove_class_BANG___3.cljs$lang$applyTo;
remove_class_BANG_.cljs$lang$arity$2 = remove_class_BANG___2;
remove_class_BANG_.cljs$lang$arity$variadic = remove_class_BANG___3.cljs$lang$arity$variadic;
return remove_class_BANG_;
})()
;
/**
* (toggle-class! elem class) will add-class! if elem does not have class
* and remove-class! otherwise.
* (toggle-class! elem class add?) will add-class! if add? is truthy,
* otherwise it will remove-class!
*/
dommy.attrs.toggle_class_BANG_ = (function() {
var toggle_class_BANG_ = null;
var toggle_class_BANG___2 = (function (elem,class$){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var temp__3971__auto___5150 = elem__$1.classList;
if(cljs.core.truth_(temp__3971__auto___5150))
{var class_list_5151 = temp__3971__auto___5150;
class_list_5151.toggle(class$);
} else
{toggle_class_BANG_.call(null,elem__$1,class$,!(dommy.attrs.has_class_QMARK_.call(null,elem__$1,class$)));
}
return elem__$1;
});
var toggle_class_BANG___3 = (function (elem,class$,add_QMARK_){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
if(add_QMARK_)
{dommy.attrs.add_class_BANG_.call(null,elem__$1,class$);
} else
{dommy.attrs.remove_class_BANG_.call(null,elem__$1,class$);
}
return elem__$1;
});
toggle_class_BANG_ = function(elem,class$,add_QMARK_){
switch(arguments.length){
case 2:
return toggle_class_BANG___2.call(this,elem,class$);
case 3:
return toggle_class_BANG___3.call(this,elem,class$,add_QMARK_);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
toggle_class_BANG_.cljs$lang$arity$2 = toggle_class_BANG___2;
toggle_class_BANG_.cljs$lang$arity$3 = toggle_class_BANG___3;
return toggle_class_BANG_;
})()
;
dommy.attrs.style_str = (function style_str(x){
if(cljs.core.string_QMARK_.call(null,x))
{return x;
} else
{return clojure.string.join.call(null," ",cljs.core.map.call(null,(function (p__5154){
var vec__5155 = p__5154;
var k = cljs.core.nth.call(null,vec__5155,0,null);
var v = cljs.core.nth.call(null,vec__5155,1,null);
return [cljs.core.str(cljs.core.name.call(null,k)),cljs.core.str(":"),cljs.core.str(cljs.core.name.call(null,v)),cljs.core.str(";")].join('');
}),x));
}
});
/**
* @param {...*} var_args
*/
dommy.attrs.set_style_BANG_ = (function() { 
var set_style_BANG___delegate = function (elem,kvs){
if(cljs.core.even_QMARK_.call(null,cljs.core.count.call(null,kvs)))
{} else
{throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.with_meta(cljs.core.list("\uFDD1'even?",cljs.core.with_meta(cljs.core.list("\uFDD1'count","\uFDD1'kvs"),cljs.core.hash_map("\uFDD0'line",120))),cljs.core.hash_map("\uFDD0'line",120))))].join('')));
}
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var style = elem__$1.style;
var G__5158_5160 = cljs.core.seq.call(null,cljs.core.partition.call(null,2,kvs));
while(true){
if(G__5158_5160)
{var vec__5159_5161 = cljs.core.first.call(null,G__5158_5160);
var k_5162 = cljs.core.nth.call(null,vec__5159_5161,0,null);
var v_5163 = cljs.core.nth.call(null,vec__5159_5161,1,null);
(style[cljs.core.name.call(null,k_5162)] = v_5163);
{
var G__5164 = cljs.core.next.call(null,G__5158_5160);
G__5158_5160 = G__5164;
continue;
}
} else
{}
break;
}
return elem__$1;
};
var set_style_BANG_ = function (elem,var_args){
var kvs = null;
if (goog.isDef(var_args)) {
  kvs = cljs.core.array_seq(Array.prototype.slice.call(arguments, 1),0);
} 
return set_style_BANG___delegate.call(this, elem, kvs);
};
set_style_BANG_.cljs$lang$maxFixedArity = 1;
set_style_BANG_.cljs$lang$applyTo = (function (arglist__5165){
var elem = cljs.core.first(arglist__5165);
var kvs = cljs.core.rest(arglist__5165);
return set_style_BANG___delegate(elem, kvs);
});
set_style_BANG_.cljs$lang$arity$variadic = set_style_BANG___delegate;
return set_style_BANG_;
})()
;
dommy.attrs.style = (function style(elem,k){
if(cljs.core.truth_(k))
{} else
{throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,"\uFDD1'k"))].join('')));
}
return (window.getComputedStyle(dommy.template.__GT_node_like.call(null,elem))[cljs.core.name.call(null,k)]);
});
/**
* @param {...*} var_args
*/
dommy.attrs.set_px_BANG_ = (function() { 
var set_px_BANG___delegate = function (elem,kvs){
if(cljs.core.even_QMARK_.call(null,cljs.core.count.call(null,kvs)))
{} else
{throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.with_meta(cljs.core.list("\uFDD1'even?",cljs.core.with_meta(cljs.core.list("\uFDD1'count","\uFDD1'kvs"),cljs.core.hash_map("\uFDD0'line",132))),cljs.core.hash_map("\uFDD0'line",132))))].join('')));
}
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var G__5168_5170 = cljs.core.seq.call(null,cljs.core.partition.call(null,2,kvs));
while(true){
if(G__5168_5170)
{var vec__5169_5171 = cljs.core.first.call(null,G__5168_5170);
var k_5172 = cljs.core.nth.call(null,vec__5169_5171,0,null);
var v_5173 = cljs.core.nth.call(null,vec__5169_5171,1,null);
dommy.attrs.set_style_BANG_.call(null,elem__$1,k_5172,[cljs.core.str(v_5173),cljs.core.str("px")].join(''));
{
var G__5174 = cljs.core.next.call(null,G__5168_5170);
G__5168_5170 = G__5174;
continue;
}
} else
{}
break;
}
return elem__$1;
};
var set_px_BANG_ = function (elem,var_args){
var kvs = null;
if (goog.isDef(var_args)) {
  kvs = cljs.core.array_seq(Array.prototype.slice.call(arguments, 1),0);
} 
return set_px_BANG___delegate.call(this, elem, kvs);
};
set_px_BANG_.cljs$lang$maxFixedArity = 1;
set_px_BANG_.cljs$lang$applyTo = (function (arglist__5175){
var elem = cljs.core.first(arglist__5175);
var kvs = cljs.core.rest(arglist__5175);
return set_px_BANG___delegate(elem, kvs);
});
set_px_BANG_.cljs$lang$arity$variadic = set_px_BANG___delegate;
return set_px_BANG_;
})()
;
dommy.attrs.px = (function px(elem,k){
var pixels = dommy.attrs.style.call(null,dommy.template.__GT_node_like.call(null,elem),k);
if(cljs.core.seq.call(null,pixels))
{return parseInt(pixels);
} else
{return null;
}
});
/**
* Sets dom attributes on and returns `elem`.
* Attributes without values will be set to "true":
* 
* (set-attr! elem :disabled)
* 
* With values, the function takes variadic kv pairs:
* 
* (set-attr! elem :id "some-id"
* :name "some-name")
* @param {...*} var_args
*/
dommy.attrs.set_attr_BANG_ = (function() {
var set_attr_BANG_ = null;
var set_attr_BANG___2 = (function (elem,k){
return set_attr_BANG_.call(null,dommy.template.__GT_node_like.call(null,elem),k,"true");
});
var set_attr_BANG___3 = (function (elem,k,v){
if(cljs.core.truth_(v))
{if(cljs.core.fn_QMARK_.call(null,v))
{var G__5180 = dommy.template.__GT_node_like.call(null,elem);
(G__5180[cljs.core.name.call(null,k)] = v);
return G__5180;
} else
{var G__5181 = dommy.template.__GT_node_like.call(null,elem);
G__5181.setAttribute(cljs.core.name.call(null,k),(((k === "\uFDD0'style"))?dommy.attrs.style_str.call(null,v):v));
return G__5181;
}
} else
{return null;
}
});
var set_attr_BANG___4 = (function() { 
var G__5184__delegate = function (elem,k,v,kvs){
if(cljs.core.even_QMARK_.call(null,cljs.core.count.call(null,kvs)))
{} else
{throw (new Error([cljs.core.str("Assert failed: "),cljs.core.str(cljs.core.pr_str.call(null,cljs.core.with_meta(cljs.core.list("\uFDD1'even?",cljs.core.with_meta(cljs.core.list("\uFDD1'count","\uFDD1'kvs"),cljs.core.hash_map("\uFDD0'line",166))),cljs.core.hash_map("\uFDD0'line",166))))].join('')));
}
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var G__5182_5185 = cljs.core.seq.call(null,cljs.core.cons.call(null,cljs.core.PersistentVector.fromArray([k,v], true),cljs.core.partition.call(null,2,kvs)));
while(true){
if(G__5182_5185)
{var vec__5183_5186 = cljs.core.first.call(null,G__5182_5185);
var k_5187__$1 = cljs.core.nth.call(null,vec__5183_5186,0,null);
var v_5188__$1 = cljs.core.nth.call(null,vec__5183_5186,1,null);
set_attr_BANG_.call(null,elem__$1,k_5187__$1,v_5188__$1);
{
var G__5189 = cljs.core.next.call(null,G__5182_5185);
G__5182_5185 = G__5189;
continue;
}
} else
{}
break;
}
return elem__$1;
};
var G__5184 = function (elem,k,v,var_args){
var kvs = null;
if (goog.isDef(var_args)) {
  kvs = cljs.core.array_seq(Array.prototype.slice.call(arguments, 3),0);
} 
return G__5184__delegate.call(this, elem, k, v, kvs);
};
G__5184.cljs$lang$maxFixedArity = 3;
G__5184.cljs$lang$applyTo = (function (arglist__5190){
var elem = cljs.core.first(arglist__5190);
var k = cljs.core.first(cljs.core.next(arglist__5190));
var v = cljs.core.first(cljs.core.next(cljs.core.next(arglist__5190)));
var kvs = cljs.core.rest(cljs.core.next(cljs.core.next(arglist__5190)));
return G__5184__delegate(elem, k, v, kvs);
});
G__5184.cljs$lang$arity$variadic = G__5184__delegate;
return G__5184;
})()
;
set_attr_BANG_ = function(elem,k,v,var_args){
var kvs = var_args;
switch(arguments.length){
case 2:
return set_attr_BANG___2.call(this,elem,k);
case 3:
return set_attr_BANG___3.call(this,elem,k,v);
default:
return set_attr_BANG___4.cljs$lang$arity$variadic(elem,k,v, cljs.core.array_seq(arguments, 3));
}
throw(new Error('Invalid arity: ' + arguments.length));
};
set_attr_BANG_.cljs$lang$maxFixedArity = 3;
set_attr_BANG_.cljs$lang$applyTo = set_attr_BANG___4.cljs$lang$applyTo;
set_attr_BANG_.cljs$lang$arity$2 = set_attr_BANG___2;
set_attr_BANG_.cljs$lang$arity$3 = set_attr_BANG___3;
set_attr_BANG_.cljs$lang$arity$variadic = set_attr_BANG___4.cljs$lang$arity$variadic;
return set_attr_BANG_;
})()
;
/**
* @param {...*} var_args
*/
dommy.attrs.remove_attr_BANG_ = (function() {
var remove_attr_BANG_ = null;
var remove_attr_BANG___2 = (function (elem,k){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
if(cljs.core.truth_(cljs.core.PersistentHashSet.fromArray(["\uFDD0'class","\uFDD0'classes"]).call(null,k)))
{elem__$1.className = "";
} else
{elem__$1.removeAttribute(cljs.core.name.call(null,k));
}
return elem__$1;
});
var remove_attr_BANG___3 = (function() { 
var G__5193__delegate = function (elem,k,ks){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
var G__5192_5194 = cljs.core.seq.call(null,cljs.core.cons.call(null,k,ks));
while(true){
if(G__5192_5194)
{var k_5195__$1 = cljs.core.first.call(null,G__5192_5194);
remove_attr_BANG_.call(null,elem__$1,k_5195__$1);
{
var G__5196 = cljs.core.next.call(null,G__5192_5194);
G__5192_5194 = G__5196;
continue;
}
} else
{}
break;
}
return elem__$1;
};
var G__5193 = function (elem,k,var_args){
var ks = null;
if (goog.isDef(var_args)) {
  ks = cljs.core.array_seq(Array.prototype.slice.call(arguments, 2),0);
} 
return G__5193__delegate.call(this, elem, k, ks);
};
G__5193.cljs$lang$maxFixedArity = 2;
G__5193.cljs$lang$applyTo = (function (arglist__5197){
var elem = cljs.core.first(arglist__5197);
var k = cljs.core.first(cljs.core.next(arglist__5197));
var ks = cljs.core.rest(cljs.core.next(arglist__5197));
return G__5193__delegate(elem, k, ks);
});
G__5193.cljs$lang$arity$variadic = G__5193__delegate;
return G__5193;
})()
;
remove_attr_BANG_ = function(elem,k,var_args){
var ks = var_args;
switch(arguments.length){
case 2:
return remove_attr_BANG___2.call(this,elem,k);
default:
return remove_attr_BANG___3.cljs$lang$arity$variadic(elem,k, cljs.core.array_seq(arguments, 2));
}
throw(new Error('Invalid arity: ' + arguments.length));
};
remove_attr_BANG_.cljs$lang$maxFixedArity = 2;
remove_attr_BANG_.cljs$lang$applyTo = remove_attr_BANG___3.cljs$lang$applyTo;
remove_attr_BANG_.cljs$lang$arity$2 = remove_attr_BANG___2;
remove_attr_BANG_.cljs$lang$arity$variadic = remove_attr_BANG___3.cljs$lang$arity$variadic;
return remove_attr_BANG_;
})()
;
dommy.attrs.attr = (function attr(elem,k){
if(cljs.core.truth_(k))
{return dommy.template.__GT_node_like.call(null,elem).getAttribute(cljs.core.name.call(null,k));
} else
{return null;
}
});
dommy.attrs.hidden_QMARK_ = (function hidden_QMARK_(elem){
return ("none" === dommy.template.__GT_node_like.call(null,elem).style.display);
});
/**
* Display or hide the given `elem`. Takes an optional boolean `show?`
* indicating whether to show or hide `elem`.
*/
dommy.attrs.toggle_BANG_ = (function() {
var toggle_BANG_ = null;
var toggle_BANG___1 = (function (elem){
var elem__$1 = dommy.template.__GT_node_like.call(null,elem);
toggle_BANG_.call(null,elem__$1,dommy.attrs.hidden_QMARK_.call(null,elem__$1));
return elem__$1;
});
var toggle_BANG___2 = (function (elem,show_QMARK_){
var G__5199 = dommy.template.__GT_node_like.call(null,elem);
G__5199.style.display = ((show_QMARK_)?"":"none");
return G__5199;
});
toggle_BANG_ = function(elem,show_QMARK_){
switch(arguments.length){
case 1:
return toggle_BANG___1.call(this,elem);
case 2:
return toggle_BANG___2.call(this,elem,show_QMARK_);
}
throw(new Error('Invalid arity: ' + arguments.length));
};
toggle_BANG_.cljs$lang$arity$1 = toggle_BANG___1;
toggle_BANG_.cljs$lang$arity$2 = toggle_BANG___2;
return toggle_BANG_;
})()
;
dommy.attrs.hide_BANG_ = (function hide_BANG_(elem){
var G__5201 = dommy.template.__GT_node_like.call(null,elem);
dommy.attrs.toggle_BANG_.call(null,G__5201,false);
return G__5201;
});
dommy.attrs.show_BANG_ = (function show_BANG_(elem){
var G__5203 = dommy.template.__GT_node_like.call(null,elem);
dommy.attrs.toggle_BANG_.call(null,G__5203,true);
return G__5203;
});
/**
* Returns a map of the bounding client rect of `elem`
* as a map with [:top :left :right :bottom :width :height]
*/
dommy.attrs.bounding_client_rect = (function bounding_client_rect(elem){
return cljs.core.js__GT_clj.call(null,(function (){var G__5205 = dommy.template.__GT_node_like.call(null,elem).getBoundingClientRect();
(G__5205["constructor"] = Object);
return G__5205;
})(),"\uFDD0'keywordize-keys",true);
});
