# switchpreference属性

switchpreference属性：

android:key ： 每个Preference控件独一无二的”ID”,唯一表示此Preference。          

android:defaultValue ： 默认值。 例如，CheckPreference的默认值可为”true”，默认为选中状态；

                                             EditTextPreference的默认值可为”110” 。

android:enabled ： 表示该Preference是否可用状态。     

android:title ： 每个Preference在PreferenceScreen布局上显示的标题——大标题

android:summary ： 每个Preference在PreferenceScreen布局上显示的标题——小标题(可以没有)

android:persistent： 表示Preference元素所对应的值是否写入sharedPreferen文件中，如果是true，则表示写

                                       入；否则，则表示不写入该Preference元素的值。

android:dependency： 表示一个Preference(用A表示)的可用状态依赖另外一个Preference(用B表示)。B可用，

                                              则A可用；B不可用，则A不可用。

android:disableDependentsState：  与android:dependency相反。B可用，则A不可用；B不可用，则A可用。

调用方法： 

public boolean onPreferenceClick(Preference preference)

             说明：当点击控件时触发发生，可以做相应操作。

 boolean onPreferenceChange(Preference preference,Object objValue)

             说明：  当Preference的元素值发送改变时，触发该事件。
调用步骤：
​     1 先调用onPreferenceClick()方法，如果该方法返回true，则不再调用onPreferenceTreeClick方法 ；

         如果onPreferenceClick方法返回false，则继续调用onPreferenceTreeClick方法。
    
      2 onPreferenceChange的方法独立与其他两种方法的运行。也就是说，它总是会运行。

（点击某个Preference控件后，会先回调onPreferenceChange()方法，即是否保存值，然后再回调onPreferenceClick以及onPreferenceTreeClick()方法，因此在onPreferenceClick/onPreferenceTreeClick方法中我们得到的控件值就是最新的Preference控件值。）
--------------------- 