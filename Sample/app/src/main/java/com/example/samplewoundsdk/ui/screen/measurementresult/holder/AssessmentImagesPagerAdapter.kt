package com.example.samplewoundsdk.ui.screen.measurementresult.holder

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.samplewoundsdk.data.pojo.media.MediaModel
import com.example.samplewoundsdk.ui.screen.assesmentimage.AssessmentMediaFragment

class AssessmentImagesPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val draftMediaList: ArrayList<MediaModel>,
    private val isStoma:Boolean
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() =
        draftMediaList.size

    override fun createFragment(position: Int): Fragment =
        AssessmentMediaFragment.newInstance(draftMediaList[position],isStoma)

}
